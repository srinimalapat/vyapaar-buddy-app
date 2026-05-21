package com.vyapaarbuddy.filestock;

import com.vyapaarbuddy.entity.FileStockEntry;
import com.vyapaarbuddy.entity.FileStockEntryItem;
import com.vyapaarbuddy.enums.FileStockEntryStatus;
import com.vyapaarbuddy.enums.FileStockSourceType;
import com.vyapaarbuddy.enums.FileStockType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileStockParserServiceTest {

    private FileStockParserService parser;
    private FileStockEntry stub;

    @BeforeEach
    void setUp() {
        parser = new FileStockParserService();
        stub = FileStockEntry.builder()
                .id(1L).sourceType(FileStockSourceType.LOCAL_UPLOAD)
                .fileType(FileStockType.TEXT).status(FileStockEntryStatus.PENDING_REVIEW)
                .build();
    }

    @Test
    void parse_plainText_rice25kg60() {
        List<FileStockEntryItem> items = parser.parseExtractedText("Rice 25kg 60", stub);
        assertEquals(1, items.size());
        FileStockEntryItem i = items.get(0);
        assertEquals("Rice", i.getItemName());
        assertEquals(new BigDecimal("25"), i.getQuantity());
        assertEquals("kg", i.getUnit());
        assertEquals(new BigDecimal("60"), i.getUnitPrice());
        assertEquals(0, new BigDecimal("0.90").compareTo(i.getConfidenceScore()));
        assertNull(i.getValidationErrors());
    }

    @Test
    void parse_plainText_spaceBeforeUnit() {
        List<FileStockEntryItem> items = parser.parseExtractedText("Sugar 10 kg 45", stub);
        assertEquals(1, items.size());
        assertEquals("Sugar", items.get(0).getItemName());
        assertEquals("kg", items.get(0).getUnit());
    }

    @Test
    void parse_csvRow_rice_25_kg_60() {
        List<FileStockEntryItem> items = parser.parseExtractedText("Rice,25,kg,60", stub);
        assertEquals(1, items.size());
        assertEquals("Rice", items.get(0).getItemName());
        assertEquals(new BigDecimal("25"), items.get(0).getQuantity());
        assertEquals(new BigDecimal("60"), items.get(0).getUnitPrice());
    }

    @Test
    void parse_pipeDelimited() {
        List<FileStockEntryItem> items = parser.parseExtractedText("Oil | 5 | l | 140", stub);
        assertEquals(1, items.size());
        assertEquals("Oil", items.get(0).getItemName().trim());
    }

    @Test
    void parse_rupeeSymbol() {
        List<FileStockEntryItem> items = parser.parseExtractedText("Dal 20kg ₹120", stub);
        assertEquals(1, items.size());
        assertEquals(new BigDecimal("120"), items.get(0).getUnitPrice());
    }

    @Test
    void parse_missingPrice_setsWarning() {
        List<FileStockEntryItem> items = parser.parseExtractedText("Milk 10 packets", stub);
        assertEquals(1, items.size());
        assertNull(items.get(0).getUnitPrice());
        assertNotNull(items.get(0).getValidationErrors());
        assertTrue(items.get(0).getConfidenceScore().compareTo(new BigDecimal("0.80")) < 0);
    }

    @Test
    void parse_invoiceHeaders_skipped() {
        String text = "Invoice No: 1234\nRice 25kg 60\nTotal: 1500\nSugar 10kg 45";
        List<FileStockEntryItem> items = parser.parseExtractedText(text, stub);
        assertEquals(2, items.size());
        assertEquals("Rice",  items.get(0).getItemName());
        assertEquals("Sugar", items.get(1).getItemName());
    }

    @Test
    void parse_blankLines_skipped() {
        String text = "\nRice 25kg 60\n\n\nSugar 10kg 45\n";
        assertEquals(2, parser.parseExtractedText(text, stub).size());
    }

    @Test
    void parse_multipleItems_csvFormat() {
        String csv = "Rice,25,kg,60\nSugar,10,kg,45\nOil,5,l,140";
        List<FileStockEntryItem> items = parser.parseExtractedText(csv, stub);
        assertEquals(3, items.size());
    }

    @Test
    void parse_nullText_returnsEmpty() {
        assertTrue(parser.parseExtractedText(null, stub).isEmpty());
    }

    @Test
    void parse_milk10packets30() {
        List<FileStockEntryItem> items = parser.parseExtractedText("Milk 10 packets 30", stub);
        assertEquals(1, items.size());
        assertEquals("packets", items.get(0).getUnit());
        assertEquals(new BigDecimal("30"), items.get(0).getUnitPrice());
    }
}
