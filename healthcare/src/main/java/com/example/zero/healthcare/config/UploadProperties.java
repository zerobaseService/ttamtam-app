package com.example.zero.healthcare.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {
    private String dir;
    private String urlPrefix;
    private String baseUrl;
}
