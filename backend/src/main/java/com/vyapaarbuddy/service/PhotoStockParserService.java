package com.vyapaarbuddy.service;

import com.vyapaarbuddy.entity.PhotoStockEntry;
import com.vyapaarbuddy.entity.PhotoStockEntryItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses plain text (from OCR or mock input) into PhotoStockEntryItem candidates.
 *
 * Supported formats (one item per line):
 *   Rice 25kg 60
 *   Sugar 10 kg 45
 *   Oil 5L 140
 *   Tea powder 12 pcs 90
 *   Dal 20kg ₹120
 *   Milk 10 packets 30
 *
 * Parser extracts: itemName, quantity, unit, unitPrice (optional)
 * confidenceScore:
 *   0.90 — all four fields found
 *   0.70 — name + qty + unit, no price
 *   0.50 — partial extraction
 */
@Slf4j
@Service
public class PhotoStockParserService {

    private static final Pattern ITEM_PATTERN = Pattern.compile(
            "^(.+?)\\s+(\\d+(?:\\.\\d+)?)\\s*" +
            "(kg|g|litres|litre|ltr|ml|pcs|pieces|piece|packets|packet|boxes|box|l)\\s*" +
            "[₹Rs.\\s]*(\\d+(?:\\.\\d+)?)?\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PRICE_ONLY_PATTERN = Pattern.compile(
            "^(.+?)\\s+(\\d+(?:\\.\\d+)?)\\s*[₹Rs.\\s]*(\\d+(?:\\.\\d+)?)\\s*$"
    );

    private static final List<String> SKIP_KEYWORDS = List.of(
            "invoice", "total", "date", "gst", "bill", "tax", "subtotal",
            "amount", "grand total", "discount", "no.", "no:", "serial"
    );

    public List<PhotoStockEntryItem> parseExtractedText(String extractedText, PhotoStockEntry entry) {
        List<PhotoStockEntryItem> result = new ArrayList<>();
        if (extractedText == null || extractedText.isBlank()) return result;

        String[] lines = extractedText.split("\\r?\\n");
        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isBlank()) continue;
            if (isHeaderLine(line)) continue;

            PhotoStockEntryItem item = parseLine(line, entry);
            if (item != null) result.add(item);
        }
        log.info("[PARSER] Parsed {} items from {} lines", result.size(), lines.length);
        return result;
    }

    private PhotoStockEntryItem parseLine(String line, PhotoStockEntry entry) {
        // Try full pattern: name + qty + unit + optional price
        Matcher m = ITEM_PATTERN.matcher(line);
        if (m.matches()) {
            String itemName   = m.group(1).trim();
            BigDecimal qty    = new BigDecimal(m.group(2));
            String unit       = m.group(3).toLowerCase();
            BigDecimal price  = m.group(4) != null ? new BigDecimal(m.group(4)) : null;

            BigDecimal confidence = price != null
                    ? new BigDecimal("0.90")
                    : new BigDecimal("0.70");

            String validationErrors = price == null
                    ? "Unit price not found — please enter manually"
                    : null;

            return PhotoStockEntryItem.builder()
                    .photoStockEntry(entry)
                    .itemName(itemName)
                    .quantity(qty)
                    .unit(unit)
                    .unitPrice(price)
                    .category("General")
                    .confidenceScore(confidence)
                    .validationErrors(validationErrors)
                    .build();
        }

        // Fallback: name + qty + price (no explicit unit)
        Matcher m2 = PRICE_ONLY_PATTERN.matcher(line);
        if (m2.matches()) {
            String itemName   = m2.group(1).trim();
            BigDecimal qty    = new BigDecimal(m2.group(2));
            BigDecimal price  = new BigDecimal(m2.group(3));

            return PhotoStockEntryItem.builder()
                    .photoStockEntry(entry)
                    .itemName(itemName)
                    .quantity(qty)
                    .unit(null)
                    .unitPrice(price)
                    .category("General")
                    .confidenceScore(new BigDecimal("0.70"))
                    .validationErrors("Unit not detected — please enter manually")
                    .build();
        }

        // Partial — only item name could be extracted
        if (line.matches(".*[a-zA-Z].*")) {
            return PhotoStockEntryItem.builder()
                    .photoStockEntry(entry)
                    .itemName(line)
                    .quantity(null)
                    .unit(null)
                    .unitPrice(null)
                    .category("General")
                    .confidenceScore(new BigDecimal("0.50"))
                    .validationErrors("Could not extract quantity or price — please fill in manually")
                    .build();
        }

        return null;
    }

    private boolean isHeaderLine(String line) {
        String lower = line.toLowerCase();
        return SKIP_KEYWORDS.stream().anyMatch(lower::contains);
    }
}
