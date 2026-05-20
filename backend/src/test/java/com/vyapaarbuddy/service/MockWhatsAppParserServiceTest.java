package com.vyapaarbuddy.service;

import com.vyapaarbuddy.dto.request.MockWhatsAppRequest;
import com.vyapaarbuddy.dto.response.MockCommandResponse;
import com.vyapaarbuddy.enums.CommandType;
import com.vyapaarbuddy.service.impl.MockWhatsAppParserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MockWhatsAppParserServiceTest {

    @Mock private SaleService saleService;
    @Mock private CreditService creditService;
    @Mock private InventoryService inventoryService;
    @Mock private CustomerService customerService;
    @Mock private DashboardService dashboardService;
    @Mock private com.vyapaarbuddy.security.CurrentUserService currentUserService;

    @InjectMocks
    private MockWhatsAppParserServiceImpl parserService;

    private MockCommandResponse parse(String msg) {
        MockWhatsAppRequest req = new MockWhatsAppRequest();
        req.setMessage(msg);
        return parserService.parseMessage(req);
    }

    @Test
    void parseCashSale() {
        MockCommandResponse r = parse("Sale Ramesh rice 2kg 120 cash");

        assertEquals(CommandType.CREATE_SALE, r.getCommandType());
        assertEquals("Ramesh", r.getCustomerName());
        assertEquals("rice", r.getItemName());
        assertEquals(0, BigDecimal.valueOf(2).compareTo(r.getQuantity()));
        assertEquals(0, BigDecimal.valueOf(120).compareTo(r.getAmount()));
        assertEquals("cash", r.getPaymentType());
        assertTrue(r.getValidationErrors().isEmpty());
        assertTrue(r.getExecutable());
    }

    @Test
    void parseCreditSale() {
        MockCommandResponse r = parse("Sale Amit tea 2 40 credit");

        assertEquals(CommandType.CREATE_SALE, r.getCommandType());
        assertEquals("credit", r.getPaymentType());
        assertTrue(r.getExecutable());
    }

    @Test
    void parseUdhaar() {
        MockCommandResponse r = parse("Udhaar Suresh 500");

        assertEquals(CommandType.ADD_CREDIT, r.getCommandType());
        assertEquals("Suresh", r.getCustomerName());
        assertEquals(0, BigDecimal.valueOf(500).compareTo(r.getAmount()));
        assertTrue(r.getValidationErrors().isEmpty());
        assertTrue(r.getExecutable());
    }

    @Test
    void parseCreditAlias() {
        MockCommandResponse r = parse("Credit Ramesh 850");
        assertEquals(CommandType.ADD_CREDIT, r.getCommandType());
        assertEquals("Ramesh", r.getCustomerName());
        assertEquals(0, BigDecimal.valueOf(850).compareTo(r.getAmount()));
    }

    @Test
    void parsePayment() {
        MockCommandResponse r = parse("Payment Ramesh 300");

        assertEquals(CommandType.RECORD_PAYMENT, r.getCommandType());
        assertEquals("Ramesh", r.getCustomerName());
        assertEquals(0, BigDecimal.valueOf(300).compareTo(r.getAmount()));
        assertTrue(r.getExecutable());
    }

    @Test
    void parsePaidAlias() {
        MockCommandResponse r = parse("Paid Suresh 500");
        assertEquals(CommandType.RECORD_PAYMENT, r.getCommandType());
    }

    @Test
    void parseStockAdd() {
        MockCommandResponse r = parse("Stock add sugar 10kg 45");

        assertEquals(CommandType.ADD_STOCK, r.getCommandType());
        assertEquals("sugar", r.getItemName());
        assertEquals(0, BigDecimal.valueOf(10).compareTo(r.getQuantity()));
        assertEquals(0, BigDecimal.valueOf(45).compareTo(r.getAmount()));
        assertTrue(r.getExecutable());
    }

    @Test
    void parseReportToday() {
        MockCommandResponse r = parse("Report today");

        assertEquals(CommandType.DAILY_REPORT, r.getCommandType());
        assertTrue(r.getExecutable());
        assertTrue(r.getValidationErrors().isEmpty());
    }

    @Test
    void parseTodayReport() {
        MockCommandResponse r = parse("Today report");
        assertEquals(CommandType.DAILY_REPORT, r.getCommandType());
    }

    @Test
    void parseUnknownCommand() {
        MockCommandResponse r = parse("Hello there");

        assertEquals(CommandType.UNKNOWN, r.getCommandType());
        assertFalse(r.getExecutable());
        assertFalse(r.getValidationErrors().isEmpty());
        assertTrue(r.getValidationErrors().get(0).contains("Unsupported"));
    }

    @Test
    void validationError_missingSaleAmount() {
        MockCommandResponse r = parse("Sale Ramesh rice 2kg abc cash");
        assertEquals(CommandType.UNKNOWN, r.getCommandType()); // won't match regex
        assertFalse(r.getExecutable());
    }

    @Test
    void caseInsensitive_saleUpperCase() {
        MockCommandResponse r = parse("SALE Priya Dal 1kg 80 UPI");
        assertEquals(CommandType.CREATE_SALE, r.getCommandType());
        assertEquals("upi", r.getPaymentType());
    }

    @Test
    void confidenceScore_highForValidCommands() {
        MockCommandResponse r = parse("Udhaar Suresh 500");
        assertEquals(0, new BigDecimal("0.90").compareTo(r.getConfidenceScore()));
    }

    @Test
    void confidenceScore_lowForUnknown() {
        MockCommandResponse r = parse("xyz unknown garbage");
        assertEquals(0, new BigDecimal("0.10").compareTo(r.getConfidenceScore()));
    }
}
