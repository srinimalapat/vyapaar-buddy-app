package com.vyapaarbuddy.filestock.extractor;

import com.vyapaarbuddy.enums.FileStockType;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Extracts text from PDF files using Apache PDFBox 3.x.
 *
 * Works for text-based PDFs (digital invoices, bills).
 * Scanned / image-only PDFs have no text layer — returns a helpful error.
 *
 * Future: integrate Tesseract OCR via Tess4J for scanned PDFs.
 */
@Slf4j
@Component
public class PdfFileExtractor {

    public ExtractionResult extract(MultipartFile file) {
        try {
            // PDFBox 3.x uses Loader.loadPDF() instead of PDDocument.load()
            byte[] bytes = file.getBytes();
            try (PDDocument doc = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                String text = stripper.getText(doc).trim();

                if (text.isBlank()) {
                    log.info("[EXTRACTOR] PDF has no text layer (likely scanned): {}", file.getOriginalFilename());
                    return ExtractionResult.builder()
                            .success(false)
                            .provider("PDFBOX")
                            .errorMessage("PDF text not found. This may be a scanned bill. " +
                                    "OCR provider (e.g. Tesseract) is required for scanned PDFs. " +
                                    "You can also provide mockText for local testing.")
                            .fileType(FileStockType.PDF)
                            .build();
                }

                log.info("[EXTRACTOR] PDF extracted {} chars from {}", text.length(), file.getOriginalFilename());
                return ExtractionResult.builder()
                        .success(true)
                        .extractedText(text)
                        .provider("PDFBOX")
                        .confidenceScore(new BigDecimal("0.90"))
                        .fileType(FileStockType.PDF)
                        .build();
            }
        } catch (IOException e) {
            log.warn("[EXTRACTOR] Failed to read PDF: {}", e.getMessage());
            return ExtractionResult.builder()
                    .success(false)
                    .provider("PDFBOX")
                    .errorMessage("Could not read PDF: " + e.getMessage())
                    .fileType(FileStockType.PDF)
                    .build();
        }
    }
}
