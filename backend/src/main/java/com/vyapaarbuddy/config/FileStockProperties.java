package com.vyapaarbuddy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "app.file-stock")
public class FileStockProperties {

    private String uploadDir = "uploads/file-stock";
    private int maxFileSizeMb = 10;
    private List<String> allowedContentTypes = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp",
            "application/pdf",
            "text/plain", "text/csv",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
}
