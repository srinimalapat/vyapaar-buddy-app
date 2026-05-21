package com.vyapaarbuddy.filestock;

import com.vyapaarbuddy.entity.FileStockEntry;
import com.vyapaarbuddy.entity.FileStockEntryItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses extracted text into FileStockEntryItem candidates.
 *
 * Supported line formats:
 *   Rice 25kg 60
 *   Rice 25 kg 60
 *   Sugar, 10, kg, 45          (comma-separated)
 *   Rice,25,kg,60              (CSV columns)
 *   Item | 10 | kg | 45        (pipe-separated)
 *   Oil 5L ₹140
 *   Dal 20kg Rs 120
 *   Milk 10 packets 30
 *
 * Confidence scoring:
 *   0.90 — name + qty + unit + price
 *   0.75 — name + qty + price (no unit)
 *   0.70 — name + qty + unit (no price)
 *   0.50 — partial / name only
 */
@Slf4j
@Service
public class FileStockParserService {

    // Full pattern: name qty unit [price]
    private static final Pattern FULL_PATTERN = Pattern.compile(
            "^(.+?)\\s+(\\d+(?:\\.\\d+)?)\\s*" +
            "(kg|g|litres|litre|ltr|ml|pcs|pieces|piece|packets|packet|units|unit|boxes|box|l)\\s*" +
            "(?:[₹Rrs.\\s]+)?(\\d+(?:\\.\\d+)?)?\\s*$",
            Pattern.CASE_INSENSITIVE
    );

    // Delimiter pattern: comma or pipe separated columns — spaces around delimiter are allowed
    // Matches: Rice,25,kg,60 | Rice, 25, kg, 60 | Oil | 5 | l | 140
    private static final Pattern DELIMITED_PATTERN = Pattern.compile(
            "^([^,|]+?)\\s*[,|]\\s*(\\d+(?:\\.\\d+)?)\\s*[,|]\\s*([^,|\\d]+?)?\\s*(?:[,|]\\s*[₹Rrs.\\s]*(\\d+(?:\\.\\d+)?))?\\s*$"
    );

    // Fallback: name + qty + price (no unit)
    private static final Pattern NO_UNIT_PATTERN = Pattern.compile(
            "^(.+?)\\s+(\\d+(?:\\.\\d+)?)\\s+[₹Rrs.\\s]*(\\d+(?:\\.\\d+)?)\\s*$"
    );

    private static final List<String> SKIP_KEYWORDS = List.of(
            "invoice", "bill no", "bill number", "gst", "total", "subtotal",
            "date", "phone", "address", "tax", "amount due", "grand total",
            "discount", "serial", "sr no", "s.no", "no.", "qty", "item name",
            "description", "rate", "amount", "unit price"
    );

    public List<FileStockEntryItem> parseExtractedText(String extractedText, FileStockEntry entry) {
        List<FileStockEntryItem> result = new ArrayList<>();
        if (extractedText == null || extractedText.isBlank()) return result;

        for (String rawLine : extractedText.split("\\r?\\n")) {
            String line = rawLine.trim();
            if (line.isBlank() || isHeaderLine(line)) continue;

            FileStockEntryItem item = parseLine(line, entry);
            if (item != null) result.add(item);
        }
        log.info("[PARSER] Parsed {} items from text", result.size());
        return result;
    }

    private FileStockEntryItem parseLine(String line, FileStockEntry entry) {
        // 1. Try delimiter (CSV/pipe) format first
        Matcher dm = DELIMITED_PATTERN.matcher(line);
        if (dm.matches()) {
            String name  = dm.group(1).trim();
            BigDecimal qty   = new BigDecimal(dm.group(2));
            String unit  = dm.group(3) != null ? dm.group(3).trim().toLowerCase() : null;
            BigDecimal price = dm.group(4) != null ? new BigDecimal(dm.group(4)) : null;
            return buildItem(entry, name, qty, unit, price);
        }

        // 2. Try full pattern: name qty unit [price]
        Matcher fm = FULL_PATTERN.matcher(line);
        if (fm.matches()) {
            String name  = fm.group(1).trim();
            BigDecimal qty   = new BigDecimal(fm.group(2));
            String unit  = fm.group(3).toLowerCase();
            BigDecimal price = fm.group(4) != null ? new BigDecimal(fm.group(4)) : null;
            return buildItem(entry, name, qty, unit, price);
        }

        // 3. Fallback: name qty price (no explicit unit)
        Matcher nm = NO_UNIT_PATTERN.matcher(line);
        if (nm.matches()) {
            String name  = nm.group(1).trim();
            BigDecimal qty   = new BigDecimal(nm.group(2));
            BigDecimal price = new BigDecimal(nm.group(3));
            return buildItem(entry, name, qty, null, price);
        }

        // 4. Partial: only name-like text
        if (line.matches(".*[a-zA-Z].*") && line.length() > 1) {
            return FileStockEntryItem.builder()
                    .fileStockEntry(entry)
                    .itemName(line)
                    .quantity(null).unit(null).unitPrice(null)
                    .category("General")
                    .confidenceScore(new BigDecimal("0.50"))
                    .validationErrors("Could not extract quantity or price — please fill in manually")
                    .build();
        }
        return null;
    }

    private FileStockEntryItem buildItem(FileStockEntry entry, String name,
                                          BigDecimal qty, String unit, BigDecimal price) {
        List<String> warnings = new ArrayList<>();
        BigDecimal confidence;

        if (unit != null && price != null) {
            confidence = new BigDecimal("0.90");
        } else if (unit == null && price != null) {
            confidence = new BigDecimal("0.75");
            warnings.add("Unit not detected — please enter manually");
        } else if (unit != null) {
            confidence = new BigDecimal("0.70");
            warnings.add("Unit price not found — please enter manually");
        } else {
            confidence = new BigDecimal("0.50");
            warnings.add("Unit and price not detected — please fill in manually");
        }

        if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
            warnings.add("Missing or invalid quantity");
        }

        return FileStockEntryItem.builder()
                .fileStockEntry(entry)
                .itemName(name)
                .quantity(qty)
                .unit(unit)
                .unitPrice(price)
                .category("General")
                .confidenceScore(confidence)
                .validationErrors(warnings.isEmpty() ? null : String.join("; ", warnings))
                .build();
    }

    private boolean isHeaderLine(String line) {
        String lower = line.toLowerCase();
        return SKIP_KEYWORDS.stream().anyMatch(lower::contains);
    }
}
