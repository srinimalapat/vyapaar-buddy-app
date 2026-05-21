package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.config.FileStockProperties;
import com.vyapaarbuddy.dto.request.CancelFileStockEntryRequest;
import com.vyapaarbuddy.dto.request.ConfirmFileStockEntryItemRequest;
import com.vyapaarbuddy.dto.request.ConfirmFileStockEntryRequest;
import com.vyapaarbuddy.dto.response.FileStockEntryItemResponse;
import com.vyapaarbuddy.dto.response.FileStockEntryResponse;
import com.vyapaarbuddy.entity.*;
import com.vyapaarbuddy.enums.FileStockEntryStatus;
import com.vyapaarbuddy.enums.FileStockSourceType;
import com.vyapaarbuddy.enums.InventoryStatus;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.exception.ResourceNotFoundException;
import com.vyapaarbuddy.filestock.FileStockParserService;
import com.vyapaarbuddy.filestock.extractor.ExtractionResult;
import com.vyapaarbuddy.filestock.extractor.FileTextExtractionService;
import com.vyapaarbuddy.repository.FileStockEntryItemRepository;
import com.vyapaarbuddy.repository.FileStockEntryRepository;
import com.vyapaarbuddy.repository.InventoryItemRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.FileStockEntryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class FileStockEntryServiceImpl implements FileStockEntryService {

    private final FileStockEntryRepository    entryRepository;
    private final FileStockEntryItemRepository itemRepository;
    private final InventoryItemRepository     inventoryItemRepository;
    private final FileTextExtractionService   extractionService;
    private final FileStockParserService      parserService;
    private final FileStockProperties         properties;
    private final CurrentUserService          currentUserService;

    @Override
    @Transactional
    public FileStockEntryResponse uploadAndExtract(MultipartFile file, String mockText) {
        Business business  = currentUserService.getCurrentBusiness();
        User     uploadedBy = currentUserService.getCurrentUser();

        validateFile(file);

        String storedPath = saveFile(file);
        ExtractionResult result = extractionService.extractText(file, mockText);

        if (!result.isSuccess()) {
            FileStockEntry failed = FileStockEntry.builder()
                    .business(business).uploadedBy(uploadedBy)
                    .sourceType(FileStockSourceType.LOCAL_UPLOAD)
                    .fileType(result.getFileType())
                    .originalFileName(file.getOriginalFilename())
                    .storedFilePath(storedPath)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .status(FileStockEntryStatus.FAILED)
                    .errorMessage(result.getErrorMessage())
                    .build();
            return toResponse(entryRepository.save(failed));
        }

        FileStockEntry entry = FileStockEntry.builder()
                .business(business).uploadedBy(uploadedBy)
                .sourceType(FileStockSourceType.LOCAL_UPLOAD)
                .fileType(result.getFileType())
                .originalFileName(file.getOriginalFilename())
                .storedFilePath(storedPath)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .extractedText(result.getExtractedText())
                .status(FileStockEntryStatus.PENDING_REVIEW)
                .build();

        FileStockEntry saved = entryRepository.save(entry);
        List<FileStockEntryItem> items = parserService.parseExtractedText(result.getExtractedText(), saved);
        List<FileStockEntryItem> savedItems = itemRepository.saveAll(items);
        saved.getItems().addAll(savedItems);

        log.info("[FILE-STOCK] Created entry id={} fileType={} items={} business={}",
                saved.getId(), result.getFileType(), savedItems.size(), business.getId());
        return toResponse(saved);
    }

    @Override
    public FileStockEntryResponse getEntry(Long id) {
        Long businessId = currentUserService.getCurrentBusinessId();
        FileStockEntry entry = entryRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("File stock entry not found: " + id));
        entry.getItems().addAll(itemRepository.findByFileStockEntryId(id));
        return toResponse(entry);
    }

    @Override
    public List<FileStockEntryResponse> listEntries(FileStockEntryStatus status) {
        Long businessId = currentUserService.getCurrentBusinessId();
        List<FileStockEntry> entries = (status != null)
                ? entryRepository.findByBusinessIdAndStatusOrderByCreatedAtDesc(businessId, status)
                : entryRepository.findByBusinessIdOrderByCreatedAtDesc(businessId);
        return entries.stream().map(e -> {
            e.getItems().addAll(itemRepository.findByFileStockEntryId(e.getId()));
            return toResponse(e);
        }).toList();
    }

    @Override
    @Transactional
    public FileStockEntryResponse confirmEntry(Long id, ConfirmFileStockEntryRequest request) {
        Long businessId = currentUserService.getCurrentBusinessId();
        FileStockEntry entry = entryRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("File stock entry not found: " + id));

        if (entry.getStatus() != FileStockEntryStatus.PENDING_REVIEW) {
            throw new BadRequestException(
                    "Only PENDING_REVIEW entries can be confirmed. Current status: " + entry.getStatus());
        }

        List<ConfirmFileStockEntryItemRequest> toProcess = (request != null
                && request.getItems() != null && !request.getItems().isEmpty())
                ? request.getItems()
                : toConfirmItems(itemRepository.findByFileStockEntryId(id));

        boolean updateExisting = request == null || request.isUpdateExistingItems();
        Business business = entry.getBusiness();

        for (ConfirmFileStockEntryItemRequest req : toProcess) {
            if (req.getItemName() == null || req.getItemName().isBlank()) continue;
            if (req.getQuantity() == null || req.getQuantity().compareTo(BigDecimal.ZERO) <= 0) continue;
            applyToInventory(business, req, updateExisting);
        }

        entry.setStatus(FileStockEntryStatus.CONFIRMED);
        entry.setUpdatedAt(LocalDateTime.now());
        FileStockEntry confirmed = entryRepository.save(entry);
        confirmed.getItems().addAll(itemRepository.findByFileStockEntryId(id));

        log.info("[FILE-STOCK] Confirmed entry id={} for business={}", id, businessId);
        return toResponse(confirmed);
    }

    @Override
    @Transactional
    public FileStockEntryResponse cancelEntry(Long id, CancelFileStockEntryRequest request) {
        Long businessId = currentUserService.getCurrentBusinessId();
        FileStockEntry entry = entryRepository.findByBusinessIdAndId(businessId, id)
                .orElseThrow(() -> new ResourceNotFoundException("File stock entry not found: " + id));

        if (entry.getStatus() != FileStockEntryStatus.PENDING_REVIEW) {
            throw new BadRequestException(
                    "Only PENDING_REVIEW entries can be cancelled. Current status: " + entry.getStatus());
        }

        entry.setStatus(FileStockEntryStatus.CANCELLED);
        if (request != null && request.getReason() != null && !request.getReason().isBlank()) {
            entry.setErrorMessage(request.getReason());
        }
        entry.setUpdatedAt(LocalDateTime.now());
        return toResponse(entryRepository.save(entry));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File is required and cannot be empty");
        }
        long maxBytes = (long) properties.getMaxFileSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BadRequestException(
                    "File size exceeds limit of " + properties.getMaxFileSizeMb() + " MB");
        }
        String ct = file.getContentType();
        if (ct != null && !properties.getAllowedContentTypes().isEmpty()
                && !properties.getAllowedContentTypes().contains(ct.toLowerCase())) {
            log.warn("[FILE-STOCK] Content type {} not in allow-list — proceeding anyway", ct);
        }
    }

    private void applyToInventory(Business business, ConfirmFileStockEntryItemRequest req, boolean updateExisting) {
        int qty       = req.getQuantity().intValue();
        String cat    = (req.getCategory() != null && !req.getCategory().isBlank()) ? req.getCategory() : "General";
        int threshold = (req.getLowStockThreshold() != null) ? req.getLowStockThreshold() : 5;

        inventoryItemRepository
                .findByBusinessIdAndNameIgnoreCaseAndStatus(business.getId(), req.getItemName(), InventoryStatus.ACTIVE)
                .ifPresentOrElse(
                    existing -> {
                        if (updateExisting) {
                            existing.setQuantity(existing.getQuantity() + qty);
                            if (req.getUnitPrice() != null) existing.setUnitPrice(req.getUnitPrice());
                            inventoryItemRepository.save(existing);
                            log.debug("[FILE-STOCK] Updated '{}' +{}", req.getItemName(), qty);
                        }
                    },
                    () -> {
                        BigDecimal price = req.getUnitPrice() != null ? req.getUnitPrice() : BigDecimal.ZERO;
                        inventoryItemRepository.save(InventoryItem.builder()
                                .business(business)
                                .name(req.getItemName())
                                .category(cat)
                                .unit(req.getUnit())
                                .unitPrice(price)
                                .quantity(qty)
                                .lowStockThreshold(threshold)
                                .status(InventoryStatus.ACTIVE)
                                .build());
                        log.debug("[FILE-STOCK] Created new inventory item '{}'", req.getItemName());
                    }
                );
    }

    private String saveFile(MultipartFile file) {
        try {
            Path dir = Paths.get(properties.getUploadDir());
            Files.createDirectories(dir);
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path target = dir.resolve(filename);
            file.transferTo(target);
            return target.toString();
        } catch (IOException e) {
            log.warn("[FILE-STOCK] Could not save file: {}", e.getMessage());
            return null;
        }
    }

    private List<ConfirmFileStockEntryItemRequest> toConfirmItems(List<FileStockEntryItem> items) {
        return items.stream().map(i -> {
            ConfirmFileStockEntryItemRequest r = new ConfirmFileStockEntryItemRequest();
            r.setItemName(i.getItemName());
            r.setQuantity(i.getQuantity());
            r.setUnit(i.getUnit());
            r.setUnitPrice(i.getUnitPrice());
            r.setCategory(i.getCategory());
            return r;
        }).toList();
    }

    private FileStockEntryResponse toResponse(FileStockEntry entry) {
        List<FileStockEntryItemResponse> itemResponses = entry.getItems().stream()
                .map(i -> FileStockEntryItemResponse.builder()
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

        return FileStockEntryResponse.builder()
                .id(entry.getId())
                .sourceType(entry.getSourceType())
                .fileType(entry.getFileType())
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
