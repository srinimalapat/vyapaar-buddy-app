package com.vyapaarbuddy.ocr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * Mock OCR service for local development and testing.
 *
 * This implementation does NOT call any paid OCR API.
 * For real text extraction, replace with one of:
 *   - Tesseract OCR via Tess4J (local, open-source)
 *   - Google Cloud Vision API
 *   - AWS Textract
 *   - Azure Computer Vision
 *   - OpenAI / Gemini Vision
 *
 * To use during local testing: pass mockText as a request parameter.
 * The extracted text will be used directly for item parsing.
 */
@Slf4j
@Service
public class LocalMockOcrService implements ImageOcrService {

    @Override
    public OcrResult extractText(MultipartFile file) {
        log.info("[OCR] LocalMockOcrService called for file={} size={}",
                file.getOriginalFilename(), file.getSize());
        // Real OCR not configured — caller should supply mockText parameter instead
        return OcrResult.builder()
                .success(false)
                .provider("MOCK")
                .confidenceScore(BigDecimal.ZERO)
                .errorMessage("OCR provider not configured. Please provide mockText for local testing.")
                .build();
    }

    /**
     * Convenience method: accept pre-supplied text instead of running OCR on the image.
     * Used when the caller passes mockText in the upload request.
     */
    public OcrResult extractFromMockText(String mockText) {
        log.info("[OCR] Using mock extracted text ({} chars)", mockText.length());
        return OcrResult.builder()
                .success(true)
                .extractedText(mockText)
                .provider("MOCK")
                .confidenceScore(new BigDecimal("1.00"))
                .build();
    }
}
