package com.vyapaarbuddy.dto.response;

import com.vyapaarbuddy.enums.CommandType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MockCommandResponse {

    private CommandType commandType;
    private String customerName;
    private String itemName;
    private BigDecimal quantity;
    private BigDecimal amount;
    private String paymentType;
    private String rawMessage;
    private BigDecimal confidenceScore;
    private List<String> validationErrors;
    private Boolean executable;
    private Boolean executed;
    private String executionMessage;
    private Object executionData;
}
