package com.vyapaarbuddy.service;

import com.vyapaarbuddy.config.WhatsAppProperties;
import com.vyapaarbuddy.dto.response.WhatsAppSendResponse;
import com.vyapaarbuddy.enums.WhatsAppMode;
import com.vyapaarbuddy.service.impl.WhatsAppSenderServiceImpl;
import com.vyapaarbuddy.whatsapps.CloudWhatsAppMessageSender;
import com.vyapaarbuddy.whatsapps.ManualWhatsAppMessageSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhatsAppSenderServiceTest {

    @Mock private WhatsAppProperties properties;
    @Mock private ManualWhatsAppMessageSender manualSender;
    @Mock private CloudWhatsAppMessageSender cloudSender;

    @InjectMocks
    private WhatsAppSenderServiceImpl senderService;

    @Test
    void sendTextMessage_manualMode_returnsManualResponse() {
        when(properties.getMode()).thenReturn(WhatsAppMode.MANUAL);

        WhatsAppSendResponse response = senderService.sendTextMessage("9876543210", "Hello");

        assertEquals("MANUAL", response.getProvider());
        assertEquals("READY_TO_COPY", response.getStatus());
        assertTrue(response.isSuccess());
        verify(manualSender).sendMessage("9876543210", "Hello");
    }

    @Test
    void sendTextMessage_cloudApiEnabled_delegatesToCloudSender() {
        WhatsAppProperties.CloudApi cloudApi = new WhatsAppProperties.CloudApi();
        cloudApi.setEnabled(true);
        when(properties.getCloudApi()).thenReturn(cloudApi);
        when(properties.getMode()).thenReturn(WhatsAppMode.CLOUD_API);
        WhatsAppSendResponse cloudResponse = WhatsAppSendResponse.builder()
                .success(true).provider("CLOUD_API").messageId("wamid.123").status("SENT").build();
        when(cloudSender.sendTextMessage("9876543210", "Hello")).thenReturn(cloudResponse);

        WhatsAppSendResponse response = senderService.sendTextMessage("9876543210", "Hello");

        assertEquals("CLOUD_API", response.getProvider());
        assertTrue(response.isSuccess());
        verify(cloudSender).sendTextMessage("9876543210", "Hello");
        verifyNoInteractions(manualSender);
    }

    @Test
    void sendTextMessage_cloudApiModeButNotEnabled_returnsDisabledResponse() {
        WhatsAppProperties.CloudApi cloudApi = new WhatsAppProperties.CloudApi();
        cloudApi.setEnabled(false);
        when(properties.getCloudApi()).thenReturn(cloudApi);
        when(properties.getMode()).thenReturn(WhatsAppMode.CLOUD_API);

        WhatsAppSendResponse response = senderService.sendTextMessage("9876543210", "Hello");

        assertFalse(response.isSuccess());
        assertEquals("DISABLED", response.getStatus());
        verifyNoInteractions(manualSender);
        verifyNoInteractions(cloudSender);
    }
}
