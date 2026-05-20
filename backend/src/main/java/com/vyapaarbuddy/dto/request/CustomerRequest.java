package com.vyapaarbuddy.dto.request;

import com.vyapaarbuddy.enums.CustomerStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CustomerRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number (must be 10 digits starting with 6-9)")
    private String mobileNumber;

    private String address;

    private String notes;

    @DecimalMin(value = "0.0", inclusive = true, message = "Total credit amount cannot be negative")
    private BigDecimal totalCreditAmount;

    private CustomerStatus status;
}
