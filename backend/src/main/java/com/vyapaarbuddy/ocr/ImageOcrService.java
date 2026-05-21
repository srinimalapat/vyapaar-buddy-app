package com.vyapaarbuddy.ocr;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraction for OCR providers. Swap implementations without changing service logic.
 *
 * Future providers to plug in:
 * - Tesseract OCR (local, open-source)
 * - Google Cloud Vision API
 * - AWS Textract
 * - Azure Computer Vision
 * - OpenAI / Gemini Vision (if policy allows)
 */
public interface ImageOcrService {

    OcrResult extractText(MultipartFile file);
}
