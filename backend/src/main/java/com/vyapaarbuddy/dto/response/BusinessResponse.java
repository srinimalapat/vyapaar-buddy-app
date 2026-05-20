package com.vyapaarbuddy.dto.response;

import com.vyapaarbuddy.enums.BusinessType;
import com.vyapaarbuddy.enums.PreferredLanguage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessResponse {

    private Long id;
    private String ownerName;
    private String mobileNumber;
    private String businessName;
    private BusinessType businessType;
    private String city;
    private String state;
    private PreferredLanguage preferredLanguage;
    private String address;
    private String pinCode;
    private String gstNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
