package com.vyapaarbuddy.filestock.extractor;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for file text extraction. Implementations handle specific file types.
 *
 * Future providers to plug in:
 * - Tesseract OCR (local, open-source) for image OCR and scanned PDFs
 * - Google Cloud Vision API
 * - AWS Textract
 * - Azure Computer Vision
 * - OpenAI / Gemini Vision (if policy allows)
 */
public interface FileTextExtractionService {

    ExtractionResult extractText(MultipartFile file, String mockText);
}
