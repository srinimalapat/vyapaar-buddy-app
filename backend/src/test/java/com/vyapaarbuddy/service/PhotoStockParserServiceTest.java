package com.vyapaarbuddy.service;

import com.vyapaarbuddy.entity.PhotoStockEntry;
import com.vyapaarbuddy.entity.PhotoStockEntryItem;
import com.vyapaarbuddy.enums.PhotoStockEntryStatus;
import com.vyapaarbuddy.enums.PhotoStockSourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PhotoStockParserServiceTest {

    private PhotoStockParserService parser;
    private PhotoStockEntry stubEntry;

    @BeforeEach
    void setUp() {
        parser = new PhotoStockParserService();
        stubEntry = PhotoStockEntry.builder()
                .id(1L)
                .sourceType(PhotoStockSourceType.LOCAL_UPLOAD)
                .status(PhotoStockEntryStatus.PENDING_REVIEW)
                .build();
    }

    @Test
    void parse_rice25kg60_extractsAllFields() {
        List<PhotoStockEntryItem> items = parser.parseExtractedText("Rice 25kg 60", stubEntry);

        assertEquals(1, items.size());
        PhotoStockEntryItem item = items.get(0);
        assertEquals("Rice", item.getItemName());
        assertEquals(new BigDecimal("25"), item.getQuantity());
        assertEquals("kg", item.getUnit());
        assertEquals(new BigDecimal("60"), item.getUnitPrice());
        assertEquals(0, new BigDecimal("0.90").compareTo(item.getConfidenceScore()));
        assertNull(item.getValidationErrors());
    }

    @Test
    void parse_sugar10kgWithSpace_extractsAllFields() {
        List<PhotoStockEntryItem> items = parser.parseExtractedText("Sugar 10 kg 45", stubEntry);

        assertEquals(1, items.size());
        PhotoStockEntryItem item = items.get(0);
        assertEquals("Sugar", item.getItemName());
        assertEquals(new BigDecimal("10"), item.getQuantity());
        assertEquals("kg", item.getUnit());
        assertEquals(new BigDecimal("45"), item.getUnitPrice());
    }

    @Test
    void parse_oil5L140_extractsLitreUnit() {
        List<PhotoStockEntryItem> items = parser.parseExtractedText("Oil 5L 140", stubEntry);

        assertEquals(1, items.size());
        assertEquals("Oil", items.get(0).getItemName());
        assertEquals("l", items.get(0).getUnit());
        assertEquals(new BigDecimal("5"), items.get(0).getQuantity());
        assertEquals(new BigDecimal("140"), items.get(0).getUnitPrice());
    }

    @Test
    void parse_teaPowder12pcs90_multiWordName() {
        List<PhotoStockEntryItem> items = parser.parseExtractedText("Tea powder 12 pcs 90", stubEntry);

        assertEquals(1, items.size());
        assertEquals("Tea powder", items.get(0).getItemName());
        assertEquals(new BigDecimal("12"), items.get(0).getQuantity());
        assertEquals("pcs", items.get(0).getUnit());
    }

    @Test
    void parse_priceWithRupeeSymbol_extractsPrice() {
        List<PhotoStockEntryItem> items = parser.parseExtractedText("Dal 20kg ₹120", stubEntry);

        assertEquals(1, items.size());
        assertNotNull(items.get(0).getUnitPrice());
        assertEquals(new BigDecimal("120"), items.get(0).getUnitPrice());
    }

    @Test
    void parse_missingPrice_setsWarningAndLowerConfidence() {
        List<PhotoStockEntryItem> items = parser.parseExtractedText("Milk 10 packets", stubEntry);

        assertEquals(1, items.size());
        PhotoStockEntryItem item = items.get(0);
        assertNull(item.getUnitPrice());
        assertNotNull(item.getValidationErrors());
        assertTrue(item.getConfidenceScore().compareTo(new BigDecimal("0.75")) < 0);
    }

    @Test
    void parse_blankLines_skipped() {
        String text = "\nRice 25kg 60\n\n\nSugar 10kg 45\n";
        List<PhotoStockEntryItem> items = parser.parseExtractedText(text, stubEntry);

        assertEquals(2, items.size());
    }

    @Test
    void parse_headerLines_skipped() {
        String text = "Invoice No: 1234\nRice 25kg 60\nTotal: 1500\nSugar 10kg 45";
        List<PhotoStockEntryItem> items = parser.parseExtractedText(text, stubEntry);

        assertEquals(2, items.size());
        assertEquals("Rice", items.get(0).getItemName());
        assertEquals("Sugar", items.get(1).getItemName());
    }

    @Test
    void parse_multipleItems_returnsAll() {
        String text = "Rice 25kg 60\nSugar 10kg 45\nOil 5L 140";
        List<PhotoStockEntryItem> items = parser.parseExtractedText(text, stubEntry);

        assertEquals(3, items.size());
    }

    @Test
    void parse_nullText_returnsEmpty() {
        List<PhotoStockEntryItem> items = parser.parseExtractedText(null, stubEntry);
        assertTrue(items.isEmpty());
    }

    @Test
    void parse_milk10packets30_packetUnit() {
        List<PhotoStockEntryItem> items = parser.parseExtractedText("Milk 10 packets 30", stubEntry);

        assertEquals(1, items.size());
        assertEquals("Milk", items.get(0).getItemName());
        assertEquals("packets", items.get(0).getUnit());
        assertEquals(new BigDecimal("30"), items.get(0).getUnitPrice());
    }
}
