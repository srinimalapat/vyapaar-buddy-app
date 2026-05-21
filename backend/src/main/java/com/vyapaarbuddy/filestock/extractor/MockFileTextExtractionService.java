package com.vyapaarbuddy.filestock.extractor;

import com.vyapaarbuddy.enums.FileStockType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * Dispatcher that routes extraction to the right extractor based on file content type.
 *
 * For local MVP:
 *  - If mockText is provided, it is used directly (no real extraction needed).
 *  - TXT and CSV files are read natively.
 *  - Excel (.xls/.xlsx) is extracted via Apache POI.
 *  - PDF text is extracted via Apache PDFBox.
 *  - Word (.docx) is extracted via Apache POI XWPF.
 *  - Images: real OCR not implemented — mockText required for local testing.
 *
 * Future image OCR providers to plug in:
 *  - Tesseract OCR via Tess4J (local, open-source)
 *  - Google Cloud Vision API
 *  - AWS Textract
 *  - Azure Computer Vision
 *  - OpenAI / Gemini Vision (if policy allows)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockFileTextExtractionService implements FileTextExtractionService {

    private final TextFileExtractor  textExtractor;
    private final ExcelFileExtractor excelExtractor;
    private final PdfFileExtractor   pdfExtractor;
    private final WordFileExtractor  wordExtractor;

    @Override
    public ExtractionResult extractText(MultipartFile file, String mockText) {
        // If caller supplies mock text, use it directly regardless of file type
        if (mockText != null && !mockText.isBlank()) {
            log.info("[EXTRACTOR] Using mock text ({} chars) for file={}", mockText.length(), file.getOriginalFilename());
            return ExtractionResult.builder()
                    .success(true)
                    .extractedText(mockText.trim())
                    .provider("MOCK_TEXT")
                    .confidenceScore(new BigDecimal("1.00"))
                    .fileType(resolveFileType(file))
                    .build();
        }

        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
        String fileName    = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";

        // Plain text / CSV
        if (contentType.startsWith("text/") || fileName.endsWith(".txt") || fileName.endsWith(".csv")) {
            return textExtractor.extract(file);
        }

        // Excel
        if (contentType.contains("spreadsheet") || contentType.contains("excel")
                || fileName.endsWith(".xlsx") || fileName.endsWith(".xls")) {
            return excelExtractor.extract(file);
        }

        // PDF
        if (contentType.contains("pdf") || fileName.endsWith(".pdf")) {
            return pdfExtractor.extract(file);
        }

        // Word
        if (contentType.contains("wordprocessingml") || contentType.contains("msword")
                || fileName.endsWith(".docx") || fileName.endsWith(".doc")) {
            return wordExtractor.extract(file);
        }

        // Image — OCR not yet implemented
        if (contentType.startsWith("image/")) {
            log.info("[EXTRACTOR] Image file received — OCR not configured. file={}", file.getOriginalFilename());
            return ExtractionResult.builder()
                    .success(false)
                    .provider("MOCK")
                    .fileType(FileStockType.IMAGE)
                    .errorMessage("Image OCR provider not configured. " +
                            "Please provide 'mockText' for local testing, " +
                            "or integrate Tesseract / Google Vision / AWS Textract.")
                    .build();
        }

        return ExtractionResult.builder()
                .success(false)
                .provider("MOCK")
                .fileType(FileStockType.UNKNOWN)
                .errorMessage("Unsupported file type: " + contentType +
                        ". Supported: JPG/PNG/WEBP (mockText required), PDF, Excel, CSV, Word, TXT.")
                .build();
    }

    private FileStockType resolveFileType(MultipartFile file) {
        String ct   = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (ct.startsWith("image/"))                    return FileStockType.IMAGE;
        if (ct.contains("pdf") || name.endsWith(".pdf")) return FileStockType.PDF;
        if (ct.contains("spreadsheet") || ct.contains("excel")
                || name.endsWith(".xlsx") || name.endsWith(".xls")) return FileStockType.EXCEL;
        if (ct.equals("text/csv") || name.endsWith(".csv"))         return FileStockType.CSV;
        if (ct.contains("wordprocessingml") || ct.contains("msword")
                || name.endsWith(".docx") || name.endsWith(".doc")) return FileStockType.WORD;
        if (ct.startsWith("text/") || name.endsWith(".txt"))        return FileStockType.TEXT;
        return FileStockType.UNKNOWN;
    }
}
