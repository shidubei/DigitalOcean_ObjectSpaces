package org.example.spaces.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/*
* 文件元数据模型*/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {
    //文件键（路径+文件名）
    private String key;

    //文件大小（字节）
    private Long size;

    //最后修改时间
    private Instant lastModified;

    //文件eTag
    private String eTag;

    //文件类型
    private String contentType;

    //公共访问URL
    private String publicUrl;
}
