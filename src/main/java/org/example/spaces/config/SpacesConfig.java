package org.example.spaces.config;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

/*
* DigitalOcean Spaces配置类
* 存储所有配置信息
* */
@Data
@Validated
@Configuration
@ConfigurationProperties(prefix = "digitalocean.spaces")
public class SpacesConfig {
   @NotBlank(message = "Access key is required")
    private String accessKey;

   @NotBlank(message = "Secret key is required")
    private String secretKey;

   @NotBlank(message = "Region is required")
    private String region;

   @NotBlank(message = "Bucket name is required")
    private String bucketName;

   @NotBlank(message = "Endpoint Url is required")
    private String endpointUrl;
}
