package com.vyapaarbuddy.filestock;

import com.vyapaarbuddy.enums.FileStockType;
import com.vyapaarbuddy.filestock.extractor.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

class FileTextExtractionServiceTest {

    private MockFileTextExtractionService service;

    @BeforeEach
    void setUp() {
        service = new MockFileTextExtractionService(
                new TextFileExtractor(),
                new ExcelFileExtractor(),
                new PdfFileExtractor(),
                new WordFileExtractor()
        );
    }

    @Test
    void mockText_usedDirectlyForAnyFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.jpg", "image/jpeg", "fake".getBytes());
        ExtractionResult result = service.extractText(file, "Rice 25kg 60\nSugar 10kg 45");
        assertTrue(result.isSuccess());
        assertEquals("Rice 25kg 60\nSugar 10kg 45", result.getExtractedText());
        assertEquals("MOCK_TEXT", result.getProvider());
        assertEquals(FileStockType.IMAGE, result.getFileType());
    }

    @Test
    void textFile_extractedNatively() {
        String content = "Rice 25kg 60\nSugar 10kg 45";
        MockMultipartFile file = new MockMultipartFile(
                "file", "items.txt", "text/plain", content.getBytes());
        ExtractionResult result = service.extractText(file, null);
        assertTrue(result.isSuccess());
        assertEquals(content, result.getExtractedText());
        assertEquals(FileStockType.TEXT, result.getFileType());
    }

    @Test
    void csvFile_extractedNatively() {
        String content = "Rice,25,kg,60\nSugar,10,kg,45";
        MockMultipartFile file = new MockMultipartFile(
                "file", "stock.csv", "text/csv", content.getBytes());
        ExtractionResult result = service.extractText(file, null);
        assertTrue(result.isSuccess());
        assertEquals(FileStockType.CSV, result.getFileType());
    }

    @Test
    void imageFile_noMockText_returnsHelpfulError() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bill.png", "image/png", "fake-image-bytes".getBytes());
        ExtractionResult result = service.extractText(file, null);
        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("mockText") || result.getErrorMessage().contains("OCR"));
        assertEquals(FileStockType.IMAGE, result.getFileType());
    }

    @Test
    void unsupportedFileType_returnsHelpfulError() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "archive.zip", "application/zip", "data".getBytes());
        ExtractionResult result = service.extractText(file, null);
        assertFalse(result.isSuccess());
        assertEquals(FileStockType.UNKNOWN, result.getFileType());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    void mockText_overridesDocumentExtraction() {
        String content = "some text in the document";
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.txt", "text/plain", content.getBytes());
        ExtractionResult result = service.extractText(file, "Rice 25kg 60");
        assertEquals("Rice 25kg 60", result.getExtractedText());
        assertEquals("MOCK_TEXT", result.getProvider());
    }
}
