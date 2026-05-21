package com.vyapaarbuddy.filestock.extractor;

import com.vyapaarbuddy.enums.FileStockType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Extracts text from plain text (.txt) and CSV (.csv) files by reading raw bytes.
 * No external dependencies needed.
 */
@Slf4j
@Component
public class TextFileExtractor {

    public ExtractionResult extract(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            FileStockType type = isCsv(file) ? FileStockType.CSV : FileStockType.TEXT;
            log.info("[EXTRACTOR] Text/CSV extracted {} chars from {}", content.length(), file.getOriginalFilename());
            return ExtractionResult.builder()
                    .success(true)
                    .extractedText(content.trim())
                    .provider("TEXT_READER")
                    .confidenceScore(new BigDecimal("1.00"))
                    .fileType(type)
                    .build();
        } catch (IOException e) {
            log.warn("[EXTRACTOR] Failed to read text file: {}", e.getMessage());
            return ExtractionResult.builder()
                    .success(false)
                    .provider("TEXT_READER")
                    .errorMessage("Could not read file: " + e.getMessage())
                    .fileType(FileStockType.TEXT)
                    .build();
        }
    }

    private boolean isCsv(MultipartFile file) {
        String ct = file.getContentType();
        String name = file.getOriginalFilename();
        return "text/csv".equalsIgnoreCase(ct)
                || (name != null && name.toLowerCase().endsWith(".csv"));
    }
}
