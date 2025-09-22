package org.example.spaces.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spaces.controller.SpacesController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

/*
* S3客户端配置类
* 创建和配置访问Digital Ocean Spaces的S3客户端*/
@Slf4j
@Configuration
@RequiredArgsConstructor
public class S3ClientConfig {
    private final SpacesConfig spacesConfig;

    /*
    * 创建S3客户端，返回配置好的S3客户端
    * */
    @Bean
    public S3Client s3Client() {
        log.info("Creating S3 client, connecting: {}",spacesConfig.getEndpointUrl());

        //创建AWS凭证
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                spacesConfig.getAccessKey(),
                spacesConfig.getSecretKey()
        );

        //构建S3客户端
        S3Client client = S3Client.builder()
                .endpointOverride(URI.create(spacesConfig.getEndpointUrl()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.US_EAST_1)
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();

        log.info("S3 client created");
        return client;
    }
}
