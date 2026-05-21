package com.vyapaarbuddy.service;

import com.vyapaarbuddy.config.FileStockProperties;
import com.vyapaarbuddy.dto.request.CancelFileStockEntryRequest;
import com.vyapaarbuddy.dto.request.ConfirmFileStockEntryItemRequest;
import com.vyapaarbuddy.dto.request.ConfirmFileStockEntryRequest;
import com.vyapaarbuddy.dto.response.FileStockEntryResponse;
import com.vyapaarbuddy.entity.*;
import com.vyapaarbuddy.enums.FileStockEntryStatus;
import com.vyapaarbuddy.enums.FileStockSourceType;
import com.vyapaarbuddy.enums.FileStockType;
import com.vyapaarbuddy.enums.InventoryStatus;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.filestock.FileStockParserService;
import com.vyapaarbuddy.filestock.extractor.ExtractionResult;
import com.vyapaarbuddy.filestock.extractor.FileTextExtractionService;
import com.vyapaarbuddy.repository.FileStockEntryItemRepository;
import com.vyapaarbuddy.repository.FileStockEntryRepository;
import com.vyapaarbuddy.repository.InventoryItemRepository;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.impl.FileStockEntryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStockEntryServiceTest {

    @Mock private FileStockEntryRepository    entryRepository;
    @Mock private FileStockEntryItemRepository itemRepository;
    @Mock private InventoryItemRepository     inventoryItemRepository;
    @Mock private FileTextExtractionService   extractionService;
    @Mock private FileStockParserService      parserService;
    @Mock private FileStockProperties         properties;
    @Mock private CurrentUserService          currentUserService;

    @InjectMocks
    private FileStockEntryServiceImpl service;

    private Business business;
    private User user;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        business = Business.builder().id(1L).name("Test Shop").build();
        user     = User.builder().id(10L).name("Owner").build();
        mockFile = new MockMultipartFile("file", "stock.txt", "text/plain", "Rice 25kg 60".getBytes());
        lenient().when(properties.getUploadDir()).thenReturn("uploads/file-stock");
        lenient().when(properties.getMaxFileSizeMb()).thenReturn(10);
        lenient().when(properties.getAllowedContentTypes()).thenReturn(List.of("text/plain"));
        lenient().when(currentUserService.getCurrentBusiness()).thenReturn(business);
        lenient().when(currentUserService.getCurrentUser()).thenReturn(user);
        lenient().when(currentUserService.getCurrentBusinessId()).thenReturn(1L);
    }

    @Test
    void upload_withMockText_createsPendingEntry() {
        ExtractionResult ok = ExtractionResult.builder()
                .success(true).extractedText("Rice 25kg 60")
                .provider("MOCK_TEXT").fileType(FileStockType.TEXT).build();
        when(extractionService.extractText(any(), eq("Rice 25kg 60"))).thenReturn(ok);
        FileStockEntry saved = buildEntry(1L, FileStockEntryStatus.PENDING_REVIEW);
        when(entryRepository.save(any())).thenReturn(saved);
        when(parserService.parseExtractedText(anyString(), any())).thenReturn(new ArrayList<>());
        when(itemRepository.saveAll(any())).thenReturn(new ArrayList<>());

        FileStockEntryResponse result = service.uploadAndExtract(mockFile, "Rice 25kg 60");

        assertNotNull(result);
        assertEquals(FileStockEntryStatus.PENDING_REVIEW, result.getStatus());
        verify(entryRepository).save(any());
    }

    @Test
    void upload_textFile_nativeExtraction_createsPendingEntry() {
        ExtractionResult ok = ExtractionResult.builder()
                .success(true).extractedText("Sugar 10kg 45")
                .provider("TEXT_READER").fileType(FileStockType.TEXT).build();
        when(extractionService.extractText(any(), isNull())).thenReturn(ok);
        FileStockEntry saved = buildEntry(2L, FileStockEntryStatus.PENDING_REVIEW);
        when(entryRepository.save(any())).thenReturn(saved);
        when(parserService.parseExtractedText(anyString(), any())).thenReturn(new ArrayList<>());
        when(itemRepository.saveAll(any())).thenReturn(new ArrayList<>());

        FileStockEntryResponse result = service.uploadAndExtract(mockFile, null);

        assertEquals(FileStockEntryStatus.PENDING_REVIEW, result.getStatus());
    }

    @Test
    void upload_extractionFails_createsFailedEntry() {
        ExtractionResult fail = ExtractionResult.builder()
                .success(false).errorMessage("OCR not configured")
                .provider("MOCK").fileType(FileStockType.IMAGE).build();
        when(extractionService.extractText(any(), isNull())).thenReturn(fail);
        FileStockEntry failedEntry = buildEntry(3L, FileStockEntryStatus.FAILED);
        failedEntry.setErrorMessage("OCR not configured");
        when(entryRepository.save(any())).thenReturn(failedEntry);

        FileStockEntryResponse result = service.uploadAndExtract(mockFile, null);

        assertEquals(FileStockEntryStatus.FAILED, result.getStatus());
        verify(parserService, never()).parseExtractedText(any(), any());
    }

    @Test
    void confirm_createsNewInventoryItem() {
        FileStockEntry entry = buildEntry(1L, FileStockEntryStatus.PENDING_REVIEW);
        when(entryRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(entry));
        when(itemRepository.findByFileStockEntryId(1L)).thenReturn(new ArrayList<>());
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(1L, "Rice", InventoryStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(entryRepository.save(any())).thenReturn(entry);

        ConfirmFileStockEntryRequest req = new ConfirmFileStockEntryRequest();
        req.setItems(List.of(buildItemReq("Rice", new BigDecimal("25"), "kg", new BigDecimal("60"))));
        req.setUpdateExistingItems(true);

        service.confirmEntry(1L, req);

        verify(inventoryItemRepository).save(argThat(inv ->
                "Rice".equals(inv.getName()) && inv.getQuantity() == 25));
    }

    @Test
    void confirm_updatesExistingInventoryItem() {
        FileStockEntry entry = buildEntry(1L, FileStockEntryStatus.PENDING_REVIEW);
        when(entryRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(entry));
        when(itemRepository.findByFileStockEntryId(1L)).thenReturn(new ArrayList<>());
        InventoryItem existing = InventoryItem.builder()
                .id(5L).name("Sugar").quantity(10).business(business)
                .unitPrice(new BigDecimal("40")).status(InventoryStatus.ACTIVE).build();
        when(inventoryItemRepository.findByBusinessIdAndNameIgnoreCaseAndStatus(1L, "Sugar", InventoryStatus.ACTIVE))
                .thenReturn(Optional.of(existing));
        when(entryRepository.save(any())).thenReturn(entry);

        ConfirmFileStockEntryRequest req = new ConfirmFileStockEntryRequest();
        req.setItems(List.of(buildItemReq("Sugar", new BigDecimal("20"), "kg", new BigDecimal("45"))));
        req.setUpdateExistingItems(true);

        service.confirmEntry(1L, req);

        assertEquals(30, existing.getQuantity());
        assertEquals(new BigDecimal("45"), existing.getUnitPrice());
        verify(inventoryItemRepository).save(existing);
    }

    @Test
    void confirm_notPendingReview_throwsBadRequest() {
        FileStockEntry entry = buildEntry(1L, FileStockEntryStatus.CONFIRMED);
        when(entryRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(entry));
        assertThrows(BadRequestException.class, () -> service.confirmEntry(1L, null));
    }

    @Test
    void cancel_pendingReview_setsCancelled() {
        FileStockEntry entry = buildEntry(1L, FileStockEntryStatus.PENDING_REVIEW);
        when(entryRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(entry));
        when(entryRepository.save(any())).thenReturn(entry);

        CancelFileStockEntryRequest req = new CancelFileStockEntryRequest();
        req.setReason("Wrong file");
        service.cancelEntry(1L, req);

        assertEquals(FileStockEntryStatus.CANCELLED, entry.getStatus());
        assertEquals("Wrong file", entry.getErrorMessage());
    }

    @Test
    void cancel_notPendingReview_throwsBadRequest() {
        FileStockEntry entry = buildEntry(1L, FileStockEntryStatus.CONFIRMED);
        when(entryRepository.findByBusinessIdAndId(1L, 1L)).thenReturn(Optional.of(entry));
        assertThrows(BadRequestException.class, () -> service.cancelEntry(1L, null));
    }

    @Test
    void upload_fileTooLarge_throwsBadRequest() {
        when(properties.getMaxFileSizeMb()).thenReturn(0); // 0 MB limit
        MockMultipartFile bigFile = new MockMultipartFile("file", "big.txt", "text/plain",
                new byte[10 * 1024 + 1]);
        assertThrows(BadRequestException.class, () -> service.uploadAndExtract(bigFile, null));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private FileStockEntry buildEntry(Long id, FileStockEntryStatus status) {
        return FileStockEntry.builder()
                .id(id).business(business).uploadedBy(user)
                .sourceType(FileStockSourceType.LOCAL_UPLOAD)
                .fileType(FileStockType.TEXT).status(status)
                .build();
    }

    private ConfirmFileStockEntryItemRequest buildItemReq(String name, BigDecimal qty, String unit, BigDecimal price) {
        ConfirmFileStockEntryItemRequest r = new ConfirmFileStockEntryItemRequest();
        r.setItemName(name); r.setQuantity(qty); r.setUnit(unit);
        r.setUnitPrice(price); r.setCategory("General");
        return r;
    }
}
