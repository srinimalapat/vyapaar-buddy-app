package com.vyapaarbuddy.service.impl;

import com.vyapaarbuddy.dto.request.CreditTransactionRequest;
import com.vyapaarbuddy.dto.request.MockWhatsAppRequest;
import com.vyapaarbuddy.dto.request.SaleItemRequest;
import com.vyapaarbuddy.dto.request.SaleRequest;
import com.vyapaarbuddy.dto.response.CustomerResponse;
import com.vyapaarbuddy.dto.response.MockCommandResponse;
import com.vyapaarbuddy.enums.CommandType;
import com.vyapaarbuddy.enums.CreditTransactionType;
import com.vyapaarbuddy.enums.SaleType;
import com.vyapaarbuddy.exception.BadRequestException;
import com.vyapaarbuddy.security.CurrentUserService;
import com.vyapaarbuddy.service.CreditService;
import com.vyapaarbuddy.service.CustomerService;
import com.vyapaarbuddy.service.DashboardService;
import com.vyapaarbuddy.service.InventoryService;
import com.vyapaarbuddy.service.MockWhatsAppParserService;
import com.vyapaarbuddy.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MockWhatsAppParserServiceImpl implements MockWhatsAppParserService {

    // Regex patterns – case-insensitive
    private static final Pattern SALE_PATTERN = Pattern.compile(
            "(?i)^(?:sale|sell)\\s+(\\S+)\\s+(\\S+)\\s+(\\d+\\s*\\w*)\\s+([\\d.]+)(?:\\s+(cash|upi|card|credit|online))?\\s*$");
    private static final Pattern UDHAAR_PATTERN = Pattern.compile(
            "(?i)^(?:udhaar|credit)\\s+(\\S+)\\s+([\\d.]+)\\s*$");
    private static final Pattern PAYMENT_PATTERN = Pattern.compile(
            "(?i)^(?:payment|paid|pay)\\s+(\\S+)\\s+([\\d.]+)\\s*$");
    private static final Pattern STOCK_PATTERN = Pattern.compile(
            "(?i)^stock\\s+add\\s+(\\S+)\\s+(\\d+\\s*\\w*)\\s+([\\d.]+)\\s*$");
    private static final Pattern REPORT_PATTERN = Pattern.compile(
            "(?i)^(?:report\\s+today|today\\s+report|report)\\s*$");

    private static final BigDecimal HIGH   = new BigDecimal("0.90");
    private static final BigDecimal MEDIUM = new BigDecimal("0.50");
    private static final BigDecimal LOW    = new BigDecimal("0.10");

    private final SaleService saleService;
    private final CreditService creditService;
    private final InventoryService inventoryService;
    private final CustomerService customerService;
    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    // ── Parse only ────────────────────────────────────────────────────────────

    @Override
    public MockCommandResponse parseMessage(MockWhatsAppRequest request) {
        String msg = request.getMessage().trim();
        Matcher m;

        if ((m = SALE_PATTERN.matcher(msg)).matches())    return parseSale(msg, m);
        if ((m = UDHAAR_PATTERN.matcher(msg)).matches())  return parseUdhaar(msg, m);
        if ((m = PAYMENT_PATTERN.matcher(msg)).matches()) return parsePayment(msg, m);
        if ((m = STOCK_PATTERN.matcher(msg)).matches())   return parseStock(msg, m);
        if (REPORT_PATTERN.matcher(msg).matches())        return parseReport(msg);
        return unknown(msg);
    }

    // ── Parse + Execute ───────────────────────────────────────────────────────

    @Override
    public MockCommandResponse executeMessage(MockWhatsAppRequest request) {
        MockCommandResponse parsed = parseMessage(request);
        if (!Boolean.TRUE.equals(parsed.getExecutable()) || !parsed.getValidationErrors().isEmpty()) {
            return parsed.toBuilder()
                    .executed(false)
                    .executionMessage("Command not executed: " + String.join("; ", parsed.getValidationErrors()))
                    .build();
        }

        try {
            return switch (parsed.getCommandType()) {
                case CREATE_SALE    -> executeSale(parsed);
                case ADD_CREDIT     -> executeCredit(parsed);
                case RECORD_PAYMENT -> executePayment(parsed);
                case ADD_STOCK      -> executeStock(parsed);
                case DAILY_REPORT   -> executeReport(parsed);
                default             -> parsed.toBuilder().executed(false)
                        .executionMessage("Unsupported command").build();
            };
        } catch (BadRequestException e) {
            return parsed.toBuilder()
                    .executed(false)
                    .executionMessage(e.getMessage())
                    .build();
        } catch (Exception e) {
            return parsed.toBuilder()
                    .executed(false)
                    .executionMessage("Execution failed: " + e.getMessage())
                    .build();
        }
    }

    // ── Parse helpers ─────────────────────────────────────────────────────────

    private MockCommandResponse parseSale(String raw, Matcher m) {
        String customerName = m.group(1);
        String itemName     = m.group(2);
        String qtyStr       = m.group(3);
        BigDecimal amount   = parseBigDecimal(m.group(4));
        String paymentType  = m.group(5) != null ? m.group(5).toLowerCase() : "cash";
        int qty             = parseQty(qtyStr);

        List<String> errors = new ArrayList<>();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) errors.add("Amount is required");

        return MockCommandResponse.builder()
                .commandType(CommandType.CREATE_SALE)
                .customerName(customerName)
                .itemName(itemName)
                .quantity(BigDecimal.valueOf(qty))
                .amount(amount)
                .paymentType(paymentType)
                .rawMessage(raw)
                .confidenceScore(errors.isEmpty() ? HIGH : MEDIUM)
                .validationErrors(errors)
                .executable(errors.isEmpty())
                .executed(false)
                .build();
    }

    private MockCommandResponse parseUdhaar(String raw, Matcher m) {
        String customerName = m.group(1);
        BigDecimal amount   = parseBigDecimal(m.group(2));

        List<String> errors = new ArrayList<>();
        if (customerName == null || customerName.isBlank()) errors.add("Customer name is required");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) errors.add("Amount is required");

        return MockCommandResponse.builder()
                .commandType(CommandType.ADD_CREDIT)
                .customerName(customerName)
                .amount(amount)
                .rawMessage(raw)
                .confidenceScore(errors.isEmpty() ? HIGH : MEDIUM)
                .validationErrors(errors)
                .executable(errors.isEmpty())
                .executed(false)
                .build();
    }

    private MockCommandResponse parsePayment(String raw, Matcher m) {
        String customerName = m.group(1);
        BigDecimal amount   = parseBigDecimal(m.group(2));

        List<String> errors = new ArrayList<>();
        if (customerName == null || customerName.isBlank()) errors.add("Customer name is required");
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) errors.add("Amount is required");

        return MockCommandResponse.builder()
                .commandType(CommandType.RECORD_PAYMENT)
                .customerName(customerName)
                .amount(amount)
                .rawMessage(raw)
                .confidenceScore(errors.isEmpty() ? HIGH : MEDIUM)
                .validationErrors(errors)
                .executable(errors.isEmpty())
                .executed(false)
                .build();
    }

    private MockCommandResponse parseStock(String raw, Matcher m) {
        String itemName   = m.group(1);
        int qty           = parseQty(m.group(2));
        BigDecimal price  = parseBigDecimal(m.group(3));

        List<String> errors = new ArrayList<>();
        if (itemName == null || itemName.isBlank()) errors.add("Item name is required");
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) errors.add("Unit price is required");

        return MockCommandResponse.builder()
                .commandType(CommandType.ADD_STOCK)
                .itemName(itemName)
                .quantity(BigDecimal.valueOf(qty))
                .amount(price)
                .rawMessage(raw)
                .confidenceScore(errors.isEmpty() ? HIGH : MEDIUM)
                .validationErrors(errors)
                .executable(errors.isEmpty())
                .executed(false)
                .build();
    }

    private MockCommandResponse parseReport(String raw) {
        return MockCommandResponse.builder()
                .commandType(CommandType.DAILY_REPORT)
                .rawMessage(raw)
                .confidenceScore(HIGH)
                .validationErrors(List.of())
                .executable(true)
                .executed(false)
                .build();
    }

    private MockCommandResponse unknown(String raw) {
        return MockCommandResponse.builder()
                .commandType(CommandType.UNKNOWN)
                .rawMessage(raw)
                .confidenceScore(LOW)
                .validationErrors(List.of(
                        "Unsupported command. Try: " +
                        "\"Sale Ramesh rice 2kg 120 cash\", " +
                        "\"Udhaar Suresh 500\", " +
                        "\"Payment Ramesh 300\", " +
                        "\"Stock add sugar 10kg 45\", " +
                        "\"Report today\""))
                .executable(false)
                .executed(false)
                .build();
    }

    // ── Execute helpers ───────────────────────────────────────────────────────

    private MockCommandResponse executeSale(MockCommandResponse cmd) {
        String paymentType = cmd.getPaymentType() != null ? cmd.getPaymentType() : "cash";
        SaleType saleType  = mapSaleType(paymentType);
        boolean isCredit   = SaleType.CREDIT.equals(saleType);
        BigDecimal amount  = cmd.getAmount();
        int qty            = cmd.getQuantity() != null ? cmd.getQuantity().intValue() : 1;

        // Find customer
        Optional<CustomerResponse> customerOpt = customerService.findCustomerByName(cmd.getCustomerName());

        if (isCredit && customerOpt.isEmpty()) {
            return cmd.toBuilder()
                    .executed(false)
                    .executionMessage("Customer not found: '" + cmd.getCustomerName()
                            + "'. Please create the customer first via POST /api/v1/customers")
                    .build();
        }

        SaleItemRequest item = new SaleItemRequest();
        item.setItemName(cmd.getItemName());
        item.setQuantity(qty);
        BigDecimal unitPrice = qty > 0
                ? amount.divide(BigDecimal.valueOf(qty), 2, RoundingMode.HALF_UP)
                : amount;
        item.setUnitPrice(unitPrice);

        SaleRequest saleReq = new SaleRequest();
        saleReq.setSaleType(saleType);
        saleReq.setTotalAmount(amount);
        saleReq.setPaidAmount(isCredit ? BigDecimal.ZERO : amount);
        saleReq.setItems(List.of(item));
        customerOpt.ifPresent(c -> saleReq.setCustomerId(c.getId()));

        Object result = saleService.createSale(saleReq);
        return cmd.toBuilder()
                .executed(true)
                .executionMessage("Sale created successfully")
                .executionData(result)
                .build();
    }

    private MockCommandResponse executeCredit(MockCommandResponse cmd) {
        Optional<CustomerResponse> customerOpt = customerService.findCustomerByName(cmd.getCustomerName());
        if (customerOpt.isEmpty()) {
            return cmd.toBuilder().executed(false)
                    .executionMessage("Customer not found: '" + cmd.getCustomerName() + "'").build();
        }
        CreditTransactionRequest req = new CreditTransactionRequest();
        req.setCustomerId(customerOpt.get().getId());
        req.setTransactionType(CreditTransactionType.CREDIT_GIVEN);
        req.setAmount(cmd.getAmount());
        req.setTransactionDate(LocalDate.now());
        req.setDescription("Via WhatsApp: " + cmd.getRawMessage());

        Object result = creditService.addCreditTransaction(req);
        return cmd.toBuilder()
                .executed(true)
                .executionMessage("Udhaar (credit) of ₹" + cmd.getAmount() + " added for " + cmd.getCustomerName())
                .executionData(result)
                .build();
    }

    private MockCommandResponse executePayment(MockCommandResponse cmd) {
        Optional<CustomerResponse> customerOpt = customerService.findCustomerByName(cmd.getCustomerName());
        if (customerOpt.isEmpty()) {
            return cmd.toBuilder().executed(false)
                    .executionMessage("Customer not found: '" + cmd.getCustomerName() + "'").build();
        }
        CreditTransactionRequest req = new CreditTransactionRequest();
        req.setCustomerId(customerOpt.get().getId());
        req.setTransactionType(CreditTransactionType.PAYMENT_RECEIVED);
        req.setAmount(cmd.getAmount());
        req.setTransactionDate(LocalDate.now());
        req.setDescription("Via WhatsApp: " + cmd.getRawMessage());

        try {
            Object result = creditService.recordPayment(req);
            return cmd.toBuilder()
                    .executed(true)
                    .executionMessage("Payment of ₹" + cmd.getAmount() + " recorded for " + cmd.getCustomerName())
                    .executionData(result)
                    .build();
        } catch (BadRequestException e) {
            return cmd.toBuilder().executed(false).executionMessage(e.getMessage()).build();
        }
    }

    private MockCommandResponse executeStock(MockCommandResponse cmd) {
        int qty = cmd.getQuantity() != null ? cmd.getQuantity().intValue() : 0;
        Object result = inventoryService.addOrUpdateStock(cmd.getItemName(), qty, cmd.getAmount());
        return cmd.toBuilder()
                .executed(true)
                .executionMessage("Stock updated for '" + cmd.getItemName() + "' (+" + qty + " units)")
                .executionData(result)
                .build();
    }

    private MockCommandResponse executeReport(MockCommandResponse cmd) {
        Long businessId = currentUserService.getCurrentBusinessId();
        Object stats = dashboardService.getDashboardStats(businessId);
        return cmd.toBuilder()
                .executed(true)
                .executionMessage("Today's dashboard stats")
                .executionData(stats)
                .build();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private SaleType mapSaleType(String paymentType) {
        return switch (paymentType.toLowerCase()) {
            case "upi"    -> SaleType.UPI;
            case "card"   -> SaleType.CARD;
            case "credit" -> SaleType.CREDIT;
            case "online" -> SaleType.ONLINE;
            default       -> SaleType.CASH;
        };
    }

    private int parseQty(String s) {
        if (s == null) return 1;
        String digits = s.replaceAll("[^\\d]", "");
        return digits.isEmpty() ? 1 : Integer.parseInt(digits);
    }

    private BigDecimal parseBigDecimal(String s) {
        if (s == null) return null;
        try { return new BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
    }
}
