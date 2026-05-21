package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.CancelPhotoStockEntryRequest;
import com.vyapaarbuddy.dto.request.ConfirmPhotoStockEntryItemRequest;
import com.vyapaarbuddy.dto.request.ConfirmPhotoStockEntryRequest;
import com.vyapaarbuddy.dto.response.PhotoStockEntryResponse;
import com.vyapaarbuddy.entity.*;
import com.vyapaarbuddy.enums.InventoryStatus;
import com.vyapaarbuddy.enums.PhotoStockEntryStatus;
import com.vyapaarbuddy.enums.PhotoStockSourceType;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.ocr.LocalMockOcrService;
import com.vyapaarbuddy.ocr.OcrResult;
import com.vyapaarbuddy.repository.InventoryItemRepository;
import com.vyapaarbuddy.repository.PhotoStockEntryItemRepository;
import com.vyapaarbuddy.repository.PhotoStockEntryRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.impl.PhotoStockEntryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoStockEntryServiceTest {

    @Mock private PhotoStockEntryRepository entryRepository;
    @Mock private PhotoStockEntryItemRepository itemRepository;
    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private LocalMockOcrService ocrService;
    @Mock private PhotoStockParserService parserService;
    @Mock private CurrentUserService currentUserService;

    @InjectMocks
    private PhotoStockEntryServiceImpl service;

    private Business business;
    private User user;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "uploadDir", "uploads/photo-stock");
        business = Business.builder().id(1L).name("Test Shop").build();
        user = User.builder().id(10L).name("Owner").build();
        mockFile = new MockMultipartFile("file", "stock.jpg", "image/jpeg", "fake-image".getBytes());
        lenient().when(currentUserService.getCurrentBusiness()).thenReturn(business);
        lenient().when(currentUserService.getCurrentUser()).thenReturn(user);
        lenient().when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
    }

    @Test
    void uploadAndExtract_withMockText_createsPendingEntry() {
        OcrResult ocrResult = OcrResult.builder().success(true).extractedText("Rice 25kg 60").provider("MOCK").build();
        when(ocrService.extractFromMockText("Rice 25kg 60")).thenReturn(ocrResult);

        PhotoStockEntry saved = buildEntry(1L, PhotoStockEntryStatus.PENDING_REVIEW);
        when(entryRepository.save(any())).thenReturn(saved);
        when(parserService.parseExtractedText(anyString(), any())).thenReturn(new ArrayList<>());
        when(itemRepository.saveAll(any())).thenReturn(new ArrayList<>());

        PhotoStockEntryResponse result = service.uploadAndExtract(mockFile, "Rice 25kg 60");

        assertNotNull(result);
        assertEquals(PhotoStockEntryStatus.PENDING_REVIEW, result.getStatus());
        verify(ocrService).extractFromMockText("Rice 25kg 60");
        verify(entryRepository).save(any());
    }

    @Test
    void uploadAndExtract_ocrFails_createsFailedEntry() {
        OcrResult failed = OcrResult.builder().success(false).errorMessage("OCR not configured").provider("MOCK").build();
        when(ocrService.extractText(any())).thenReturn(failed);

        PhotoStockEntry failedEntry = buildEntry(2L, PhotoStockEntryStatus.FAILED);
        failedEntry.setErrorMessage("OCR not configured");
        when(entryRepository.save(any())).thenReturn(failedEntry);

        PhotoStockEntryResponse result = service.uploadAndExtract(mockFile, null);

        assertEquals(PhotoStockEntryStatus.FAILED, result.getStatus());
        verify(parserService, never()).parseExtractedText(any(), any());
    }

    @Test
    void confirmEntry_createsNewInventoryItem() {
        PhotoStockEntry entry = buildEntry(1L, PhotoStockEntryStatus.PENDING_REVIEW);
        when(entryRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(entry));
        when(itemRepository.findByPhotoStockEntryId(1L)).thenReturn(new ArrayList<>());
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(1L, "Rice", InventoryStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(entryRepository.save(any())).thenReturn(entry);

        ConfirmPhotoStockEntryRequest req = new ConfirmPhotoStockEntryRequest();
        req.setItems(List.of(buildItemReq("Rice", new BigDecimal("25"), "kg", new BigDecimal("60"))));
        req.setUpdateExistingItems(true);

        PhotoStockEntryResponse result = service.confirmEntry(1L, req);

        assertNotNull(result);
        verify(inventoryItemRepository).save(argThat(inv ->
                "Rice".equals(inv.getName()) && inv.getQuantity() == 25));
    }

    @Test
    void confirmEntry_updatesExistingInventoryItem() {
        PhotoStockEntry entry = buildEntry(1L, PhotoStockEntryStatus.PENDING_REVIEW);
        when(entryRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(entry));
        when(itemRepository.findByPhotoStockEntryId(1L)).thenReturn(new ArrayList<>());

        InventoryItem existing = InventoryItem.builder()
                .id(5L).name("Sugar").quantity(10).business(business)
                .unitPrice(new BigDecimal("40")).status(InventoryStatus.ACTIVE).build();
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(1L, "Sugar", InventoryStatus.ACTIVE))
                .thenReturn(Optional.of(existing));
        when(entryRepository.save(any())).thenReturn(entry);

        ConfirmPhotoStockEntryRequest req = new ConfirmPhotoStockEntryRequest();
        req.setItems(List.of(buildItemReq("Sugar", new BigDecimal("20"), "kg", new BigDecimal("45"))));
        req.setUpdateExistingItems(true);

        service.confirmEntry(1L, req);

        assertEquals(30, existing.getQuantity());
        assertEquals(new BigDecimal("45"), existing.getUnitPrice());
        verify(inventoryItemRepository).save(existing);
    }

    @Test
    void confirmEntry_notPendingReview_throwsBadRequest() {
        PhotoStockEntry entry = buildEntry(1L, PhotoStockEntryStatus.CONFIRMED);
        when(entryRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(entry));

        assertThrows(BadRequestException.class, () -> service.confirmEntry(1L, null));
    }

    @Test
    void cancelEntry_pendingReview_setsCancelled() {
        PhotoStockEntry entry = buildEntry(1L, PhotoStockEntryStatus.PENDING_REVIEW);
        when(entryRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(entry));
        when(entryRepository.save(any())).thenReturn(entry);

        CancelPhotoStockEntryRequest req = new CancelPhotoStockEntryRequest();
        req.setReason("Duplicate upload");

        PhotoStockEntryResponse result = service.cancelEntry(1L, req);

        assertNotNull(result);
        assertEquals(PhotoStockEntryStatus.CANCELLED, entry.getStatus());
        assertEquals("Duplicate upload", entry.getErrorMessage());
    }

    @Test
    void cancelEntry_notPendingReview_throwsBadRequest() {
        PhotoStockEntry entry = buildEntry(1L, PhotoStockEntryStatus.CONFIRMED);
        when(entryRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(entry));

        assertThrows(BadRequestException.class, () -> service.cancelEntry(1L, null));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private PhotoStockEntry buildEntry(Long id, PhotoStockEntryStatus status) {
        return PhotoStockEntry.builder()
                .id(id)
                .business(business)
                .uploadedBy(user)
                .sourceType(PhotoStockSourceType.LOCAL_UPLOAD)
                .status(status)
                .build();
    }

    private ConfirmPhotoStockEntryItemRequest buildItemReq(String name, BigDecimal qty, String unit, BigDecimal price) {
        ConfirmPhotoStockEntryItemRequest r = new ConfirmPhotoStockEntryItemRequest();
        r.setItemName(name);
        r.setQuantity(qty);
        r.setUnit(unit);
        r.setUnitPrice(price);
        r.setCategory("General");
        return r;
    }
}
