package com.vyapaarbuddy.config;

import com.vyapaarbuddy.enums.WhatsAppMode;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.whatsapp")
public class WhatsAppProperties {

    private WhatsAppMode mode = WhatsAppMode.MANUAL;

    private CloudApi cloudApi = new CloudApi();

    private Webhook webhook = new Webhook();

    @Data
    public static class CloudApi {
        private boolean enabled = false;
        private String baseUrl = "https://graph.facebook.com";
        private String apiVersion = "v20.0";
        private String phoneNumberId;
        private String accessToken;
        private String webhookVerifyToken = "local_verify_token";
        private String businessAccountId;
    }

    @Data
    public static class Webhook {
        private boolean autoExecuteCommands = false;
    }
}
