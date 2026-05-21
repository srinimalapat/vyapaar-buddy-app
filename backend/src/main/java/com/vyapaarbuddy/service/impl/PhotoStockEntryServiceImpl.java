package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.dto.request.CancelPhotoStockEntryRequest;
import com.vyapaarbuddy.dto.request.ConfirmPhotoStockEntryItemRequest;
import com.vyapaarbuddy.dto.request.ConfirmPhotoStockEntryRequest;
import com.vyapaarbuddy.dto.response.PhotoStockEntryItemResponse;
import com.vyapaarbuddy.dto.response.PhotoStockEntryResponse;
import com.vyapaarbuddy.entity.*;
import com.vyapaarbuddy.enums.InventoryStatus;
import com.vyapaarbuddy.enums.PhotoStockEntryStatus;
import com.vyapaarbuddy.enums.PhotoStockSourceType;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.ocr.LocalMockOcrService;
import com.vyapaarbuddy.ocr.OcrResult;
import com.vyapaarbuddy.repository.InventoryItemRepository;
import com.vyapaarbuddy.repository.PhotoStockEntryItemRepository;
import com.vyapaarbuddy.repository.PhotoStockEntryRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.PhotoStockEntryService;
import com.vyapaarbuddy.service.PhotoStockParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoStockEntryServiceImpl implements PhotoStockEntryService {

    private final PhotoStockEntryRepository entryRepository;
    private final PhotoStockEntryItemRepository itemRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final LocalMockOcrService ocrService;
    private final PhotoStockParserService parserService;
    private final CurrentUserService currentUserService;

    @Value("${app.ocr.upload-dir:uploads/photo-stock}")
    private String uploadDir;

    @Override
    @Transactional
    public PhotoStockEntryResponse uploadAndExtract(MultipartFile file, String mockText) {
        Business business = currentUserService.getCurrentBusiness();
        User uploadedBy   = currentUserService.getCurrentUser();

        String storedPath = saveFile(file);

        // Run OCR — if mockText is supplied, skip actual OCR
        OcrResult ocrResult = (mockText != null && !mockText.isBlank())
                ? ocrService.extractFromMockText(mockText)
                : ocrService.extractText(file);

        if (!ocrResult.isSuccess()) {
            PhotoStockEntry failed = PhotoStockEntry.builder()
                    .business(business)
                    .uploadedBy(uploadedBy)
                    .sourceType(PhotoStockSourceType.LOCAL_UPLOAD)
                    .originalFileName(file.getOriginalFilename())
                    .storedFilePath(storedPath)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .status(PhotoStockEntryStatus.FAILED)
                    .errorMessage(ocrResult.getErrorMessage())
                    .build();
            return toResponse(entryRepository.save(failed));
        }

        // Build entry (without items first so we have the ID)
        PhotoStockEntry entry = PhotoStockEntry.builder()
                .business(business)
                .uploadedBy(uploadedBy)
                .sourceType(PhotoStockSourceType.LOCAL_UPLOAD)
                .originalFileName(file.getOriginalFilename())
                .storedFilePath(storedPath)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .extractedText(ocrResult.getExtractedText())
                .status(PhotoStockEntryStatus.PENDING_REVIEW)
                .build();

        PhotoStockEntry saved = entryRepository.save(entry);

        // Parse and save items
        List<PhotoStockEntryItem> items = parserService.parseExtractedText(ocrResult.getExtractedText(), saved);
        List<PhotoStockEntryItem> savedItems = itemRepository.saveAll(items);
        saved.getItems().addAll(savedItems);

        log.info("[PHOTO-STOCK] Created entry id={} with {} items for business={}",
                saved.getId(), savedItems.size(), business.getId());
        return toResponse(saved);
    }

    @Override
    public PhotoStockEntryResponse getEntry(Long id) {
        Long businessId = currentUserService.getCurrentBusinessId();
        PhotoStockEntry entry = entryRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo stock entry not found: " + id));
        entry.getItems().addAll(itemRepository.findByPhotoStockEntryId(id));
        return toResponse(entry);
    }

    @Override
    public List<PhotoStockEntryResponse> listEntries(PhotoStockEntryStatus status) {
        Long businessId = currentUserService.getCurrentBusinessId();
        List<PhotoStockEntry> entries = (status != null)
                ? entryRepository.findByBusinessIdAndStatusOrderByCreatedAtDesc(businessId, status)
                : entryRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
        return entries.stream().map(e -> {
            e.getItems().addAll(itemRepository.findByPhotoStockEntryId(e.getId()));
            return toResponse(e);
        }).toList();
    }

    @Override
    @Transactional
    public PhotoStockEntryResponse confirmEntry(Long id, ConfirmPhotoStockEntryRequest request) {
        Long businessId = currentUserService.getCurrentBusinessId();
        PhotoStockEntry entry = entryRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo stock entry not found: " + id));

        if (entry.getStatus() != PhotoStockEntryStatus.PENDING_REVIEW) {
            throw new BadRequestException("Only PENDING_REVIEW entries can be confirmed. Current status: " + entry.getStatus());
        }

        // Determine items to process: edited items (if provided) or original extracted items
        List<ConfirmPhotoStockEntryItemRequest> itemsToProcess = (request != null
                && request.getItems() != null
                && !request.getItems().isEmpty())
                ? request.getItems()
                : toConfirmItems(itemRepository.findByPhotoStockEntryId(id));

        boolean updateExisting = request == null || request.isUpdateExistingItems();

        Business business = entry.getBusiness();
        for (ConfirmPhotoStockEntryItemRequest itemReq : itemsToProcess) {
            if (itemReq.getItemName() == null || itemReq.getItemName().isBlank()) continue;
            if (itemReq.getQuantity() == null || itemReq.getQuantity().compareTo(BigDecimal.ZERO) <= 0) continue;

            applyToInventory(business, itemReq, updateExisting);
        }

        entry.setStatus(PhotoStockEntryStatus.CONFIRMED);
        entry.setUpdatedAt(LocalDateTime.now());
        PhotoStockEntry confirmed = entryRepository.save(entry);
        confirmed.getItems().addAll(itemRepository.findByPhotoStockEntryId(id));

        log.info("[PHOTO-STOCK] Confirmed entry id={} for business={}", id, businessId);
        return toResponse(confirmed);
    }

    @Override
    @Transactional
    public PhotoStockEntryResponse cancelEntry(Long id, CancelPhotoStockEntryRequest request) {
        Long businessId = currentUserService.getCurrentBusinessId();
        PhotoStockEntry entry = entryRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("Photo stock entry not found: " + id));

        if (entry.getStatus() != PhotoStockEntryStatus.PENDING_REVIEW) {
            throw new BadRequestException("Only PENDING_REVIEW entries can be cancelled. Current status: " + entry.getStatus());
        }

        entry.setStatus(PhotoStockEntryStatus.CANCELLED);
        if (request != null && request.getReason() != null && !request.getReason().isBlank()) {
            entry.setErrorMessage(request.getReason());
        }
        entry.setUpdatedAt(LocalDateTime.now());
        return toResponse(entryRepository.save(entry));
    }

    // ── helpers ─────────────────────────────────────────────────────────────────

    private void applyToInventory(Business business,
                                   ConfirmPhotoStockEntryItemRequest req,
                                   boolean updateExisting) {
        int qty = req.getQuantity().intValue();
        String category = (req.getCategory() != null && !req.getCategory().isBlank()) ? req.getCategory() : "General";
        int threshold   = (req.getLowStockThreshold() != null) ? req.getLowStockThreshold() : 5;

        inventoryItemRepository
                .findByBusinessIdAndNameIgnoreCaseAndStatus(business.getId(), req.getItemName(), InventoryStatus.ACTIVE)
                .ifPresentOrElse(
                    existing -> {
                        if (updateExisting) {
                            existing.setQuantity(existing.getQuantity() + qty);
                            if (req.getUnitPrice() != null) existing.setUnitPrice(req.getUnitPrice());
                            inventoryItemRepository.save(existing);
                            log.debug("[PHOTO-STOCK] Updated inventory item '{}' +{}", req.getItemName(), qty);
                        }
                    },
                    () -> {
                        BigDecimal unitPrice = req.getUnitPrice() != null ? req.getUnitPrice() : BigDecimal.ZERO;
                        InventoryItem newItem = InventoryItem.builder()
                                .business(business)
                                .name(req.getItemName())
                                .category(category)
                                .unit(req.getUnit())
                                .unitPrice(unitPrice)
                                .quantity(qty)
                                .lowStockThreshold(threshold)
                                .status(InventoryStatus.ACTIVE)
                                .build();
                        inventoryItemRepository.save(newItem);
                        log.debug("[PHOTO-STOCK] Created new inventory item '{}'", req.getItemName());
                    }
                );
    }

    private String saveFile(MultipartFile file) {
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(filename);
            file.transferTo(target);
            return target.toString();
        } catch (IOException e) {
            log.warn("[PHOTO-STOCK] Could not save file: {}", e.getMessage());
            return null;
        }
    }

    private List<ConfirmPhotoStockEntryItemRequest> toConfirmItems(List<PhotoStockEntryItem> items) {
        return items.stream().map(i -> {
            ConfirmPhotoStockEntryItemRequest r = new ConfirmPhotoStockEntryItemRequest();
            r.setItemName(i.getItemName());
            r.setQuantity(i.getQuantity());
            r.setUnit(i.getUnit());
            r.setUnitPrice(i.getUnitPrice());
            r.setCategory(i.getCategory());
            return r;
        }).toList();
    }

    private PhotoStockEntryResponse toResponse(PhotoStockEntry entry) {
        List<PhotoStockEntryItemResponse> itemResponses = entry.getItems().stream()
                .map(i -> PhotoStockEntryItemResponse.builder()
                        .id(i.getId())
                        .itemName(i.getItemName())
                        .quantity(i.getQuantity())
                        .unit(i.getUnit())
                        .unitPrice(i.getUnitPrice())
                        .category(i.getCategory())
                        .confidenceScore(i.getConfidenceScore())
                        .validationErrors(i.getValidationErrors())
                        .createdAt(i.getCreatedAt())
                        .build())
                .toList();

        return PhotoStockEntryResponse.builder()
                .id(entry.getId())
                .sourceType(entry.getSourceType())
                .originalFileName(entry.getOriginalFileName())
                .extractedText(entry.getExtractedText())
                .status(entry.getStatus())
                .errorMessage(entry.getErrorMessage())
                .items(itemResponses)
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}
