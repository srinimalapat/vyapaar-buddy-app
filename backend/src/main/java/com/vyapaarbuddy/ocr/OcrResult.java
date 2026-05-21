package com.vyapaarbuddy.ocr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResult {
    private boolean success;
    private String extractedText;
    private String provider;
    private String errorMessage;
    private BigDecimal confidenceScore;
}
