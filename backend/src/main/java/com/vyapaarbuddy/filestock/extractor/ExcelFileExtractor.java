package com.vyapaarbuddy.filestock.extractor;

import com.vyapaarbuddy.enums.FileStockType;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Extracts item data from Excel files (.xls / .xlsx) using Apache POI.
 *
 * Reads the first sheet and converts each row to a space-separated line.
 * Numeric cells are converted to clean strings (no trailing .0 for whole numbers).
 */
@Slf4j
@Component
public class ExcelFileExtractor {

    public ExtractionResult extract(MultipartFile file) {
        boolean isXlsx = isXlsx(file);
        try (Workbook workbook = isXlsx
                ? new XSSFWorkbook(file.getInputStream())
                : new HSSFWorkbook(file.getInputStream())) {

            Sheet sheet = workbook.getSheetAt(0);
            List<String> lines = new ArrayList<>();

            for (Row row : sheet) {
                String line = rowToLine(row);
                if (line != null && !line.isBlank()) {
                    lines.add(line);
                }
            }

            String text = String.join("\n", lines);
            log.info("[EXTRACTOR] Excel extracted {} rows from {}", lines.size(), file.getOriginalFilename());

            return ExtractionResult.builder()
                    .success(true)
                    .extractedText(text)
                    .provider("APACHE_POI_EXCEL")
                    .confidenceScore(new BigDecimal("0.95"))
                    .fileType(FileStockType.EXCEL)
                    .build();

        } catch (IOException e) {
            log.warn("[EXTRACTOR] Failed to read Excel file: {}", e.getMessage());
            return ExtractionResult.builder()
                    .success(false)
                    .provider("APACHE_POI_EXCEL")
                    .errorMessage("Could not read Excel file: " + e.getMessage())
                    .fileType(FileStockType.EXCEL)
                    .build();
        }
    }

    private String rowToLine(Row row) {
        if (row == null) return null;
        List<String> cells = new ArrayList<>();
        for (Cell cell : row) {
            String val = cellToString(cell);
            if (val != null && !val.isBlank()) cells.add(val.trim());
        }
        return cells.isEmpty() ? null : String.join(" ", cells);
    }

    private String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> {
                double d = cell.getNumericCellValue();
                // Avoid trailing ".0" for whole numbers
                yield (d == Math.floor(d) && !Double.isInfinite(d))
                        ? String.valueOf((long) d)
                        : String.valueOf(d);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                try { yield String.valueOf(cell.getNumericCellValue()); }
                catch (Exception e) { yield cell.getStringCellValue(); }
            }
            default -> "";
        };
    }

    private boolean isXlsx(MultipartFile file) {
        String ct   = file.getContentType();
        String name = file.getOriginalFilename();
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equalsIgnoreCase(ct)
                || (name != null && name.toLowerCase().endsWith(".xlsx"));
    }
}
