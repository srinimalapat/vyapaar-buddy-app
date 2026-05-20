package com.vyapaarbuddy.dto.request;

import com.vyapaarbuddy.enums.BusinessType;
import com.vyapaarbuddy.enums.PreferredLanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class BusinessRequest {

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number (must be 10 digits starting with 6-9)")
    private String mobileNumber;

    @NotBlank(message = "Business name is required")
    private String businessName;

    @NotNull(message = "Business type is required")
    private BusinessType businessType;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "State is required")
    private String state;

    @NotNull(message = "Preferred language is required")
    private PreferredLanguage preferredLanguage;

    private String address;

    private String pinCode;

    private String gstNumber;
}
