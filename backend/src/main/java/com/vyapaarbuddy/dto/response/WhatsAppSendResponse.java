package com.vyapaarbuddy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhatsAppSendResponse {

    private boolean success;
    private String provider;
    private String messageId;
    private String status;
    private String errorMessage;
    private String rawResponse;
}
