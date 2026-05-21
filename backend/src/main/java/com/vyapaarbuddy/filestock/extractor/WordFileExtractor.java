package com.vyapaarbuddy.filestock.extractor;

import com.vyapaarbuddy.enums.FileStockType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Extracts text from Word documents (.docx) using Apache POI XWPF.
 *
 * .docx (OOXML) is fully supported.
 * .doc (legacy binary) is not supported — returns a helpful error message.
 *
 * Future: add HWPF support for legacy .doc files if needed.
 */
@Slf4j
@Component
public class WordFileExtractor {

    public ExtractionResult extract(MultipartFile file) {
        if (isLegacyDoc(file)) {
            return ExtractionResult.builder()
                    .success(false)
                    .provider("APACHE_POI_WORD")
                    .errorMessage("Legacy .doc format is not supported. Please save as .docx and re-upload.")
                    .fileType(FileStockType.WORD)
                    .build();
        }

        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            List<XWPFParagraph> paragraphs = doc.getParagraphs();
            String text = paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .filter(t -> t != null && !t.isBlank())
                    .collect(Collectors.joining("\n"))
                    .trim();

            if (text.isBlank()) {
                return ExtractionResult.builder()
                        .success(false)
                        .provider("APACHE_POI_WORD")
                        .errorMessage("Word document appears to be empty or has no text content.")
                        .fileType(FileStockType.WORD)
                        .build();
            }

            log.info("[EXTRACTOR] Word extracted {} chars from {}", text.length(), file.getOriginalFilename());
            return ExtractionResult.builder()
                    .success(true)
                    .extractedText(text)
                    .provider("APACHE_POI_WORD")
                    .confidenceScore(new BigDecimal("0.90"))
                    .fileType(FileStockType.WORD)
                    .build();

        } catch (IOException e) {
            log.warn("[EXTRACTOR] Failed to read Word file: {}", e.getMessage());
            return ExtractionResult.builder()
                    .success(false)
                    .provider("APACHE_POI_WORD")
                    .errorMessage("Could not read Word document: " + e.getMessage())
                    .fileType(FileStockType.WORD)
                    .build();
        }
    }

    private boolean isLegacyDoc(MultipartFile file) {
        String ct   = file.getContentType();
        String name = file.getOriginalFilename();
        return "application/msword".equalsIgnoreCase(ct)
                || (name != null && name.toLowerCase().endsWith(".doc")
                    && !name.toLowerCase().endsWith(".docx"));
    }
}
