package com.vyapaarbuddy.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MockWhatsAppRequest {

    @NotBlank(message = "Message is required")
    private String message;
}
