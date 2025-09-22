package org.example.spaces.config;


/*
* DigitalOcean Spaces配置类
* 存储所有配置信息
* */
public class SpacesConfig {
    // Spaces的访问密钥
    public static final String ACCESS_KEY = "";
    // Spaces的秘密密钥
    public static final String SECRET_KEY = "";

    // Spaces的区域
    public static final String REGION = "";

    // Spaces的存储桶标签
    public static final String BUCKET_NAME = "";

    // Spaces的端点URL模板
    public static final String ENDPOINT_URL = "";

    public static String getEndpointUrl() {
        return String.format(ENDPOINT_URL,REGION);
    }
}
