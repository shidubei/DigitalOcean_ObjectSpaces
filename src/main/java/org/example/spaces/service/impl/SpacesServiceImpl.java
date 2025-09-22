package org.example.spaces.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spaces.config.SpacesConfig;
import org.example.spaces.exception.FileStorageException;
import org.example.spaces.model.FileMetadata;
import org.example.spaces.service.SpacesService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class SpacesServiceImpl implements SpacesService {
    private final S3Client s3Client;
    private final SpacesConfig spacesConfig;

    @Override
    public FileMetadata uploadFile(MultipartFile file, String folder) {
        // 验证文件
        if (file.isEmpty()) {
            throw new FileStorageException("上传文件不能为空");
        }

        try {
            // 生成唯一文件名
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileName = UUID.randomUUID().toString() + "_" + originalFilename;

            // 构建文件键
            String key = StringUtils.hasText(folder)
                    ? folder + "/" + fileName
                    : fileName;

            log.info("开始上传文件: {} -> {}", originalFilename, key);

            // 创建上传请求
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(spacesConfig.getBucketName())
                    .key(key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // 执行上传
            PutObjectResponse response = s3Client.putObject(
                    putRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize())
            );

            log.info("文件上传成功，ETag: {}", response.eTag());

            // 返回文件元数据
            return FileMetadata.builder()
                    .key(key)
                    .size(file.getSize())
                    .lastModified(java.time.Instant.now())
                    .eTag(response.eTag())
                    .contentType(file.getContentType())
                    .publicUrl(generatePublicUrl(key))
                    .build();

        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage());
            throw new FileStorageException("文件上传失败: " + e.getMessage());
        } catch (S3Exception e) {
            log.error("S3操作失败: {}", e.getMessage());
            throw new FileStorageException("S3操作失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(String key){
        try {
            log.info("开始下载文件: {}", key);

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(spacesConfig.getBucketName())
                    .key(key)
                    .build();

            return s3Client.getObject(getRequest);

        } catch (NoSuchKeyException e) {
            log.error("文件不存在: {}", key);
            throw new FileStorageException("文件不存在: " + key);
        } catch (S3Exception e) {
            log.error("下载文件失败: {}", e.getMessage());
            throw new FileStorageException("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public FileMetadata getFileMetadata(String key){
        try {
            log.info("获取文件元数据: {}", key);

            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(spacesConfig.getBucketName())
                    .key(key)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headRequest);

            return FileMetadata.builder()
                    .key(key)
                    .size(response.contentLength())
                    .lastModified(response.lastModified())
                    .eTag(response.eTag())
                    .contentType(response.contentType())
                    .publicUrl(generatePublicUrl(key))
                    .build();

        } catch (NoSuchKeyException e) {
            log.error("文件不存在: {}", key);
            throw new FileStorageException("文件不存在: " + key);
        } catch (S3Exception e) {
            log.error("获取文件元数据失败: {}", e.getMessage());
            throw new FileStorageException("获取文件元数据失败: " + e.getMessage());
        }
    }

    @Override
    public List<FileMetadata> listFiles(String prefix){
        try {
            log.info("列出文件，前缀: {}", prefix);

            ListObjectsV2Request.Builder requestBuilder = ListObjectsV2Request.builder()
                    .bucket(spacesConfig.getBucketName());

            // 添加前缀过滤
            if (StringUtils.hasText(prefix)) {
                requestBuilder.prefix(prefix);
            }

            ListObjectsV2Response response = s3Client.listObjectsV2(requestBuilder.build());

            return response.contents().stream()
                    .map(obj -> FileMetadata.builder()
                            .key(obj.key())
                            .size(obj.size())
                            .lastModified(obj.lastModified())
                            .eTag(obj.eTag())
                            .publicUrl(generatePublicUrl(obj.key()))
                            .build())
                    .collect(Collectors.toList());

        } catch (S3Exception e) {
            log.error("列出文件失败: {}", e.getMessage());
            throw new FileStorageException("列出文件失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String key){
        try{
            log.info("删除文件：{}",key);
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(spacesConfig.getBucketName())
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("删除成功：{}",key);
            return true;
        }catch(S3Exception e){
            log.error("删除文件失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean fileExists(String key){
        try{
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(spacesConfig.getBucketName())
                    .key(key)
                    .build();
            s3Client.headObject(headRequest);
            return true;
        }catch(NoSuchKeyException e){
            return false;
        }catch(S3Exception e){
            log.error("检查文件存在性失败 {}",e.getMessage());
            throw new FileStorageException("检查文件存在失败"+e.getMessage());
        }
    }

    @Override
    public String generatePublicUrl(String key){
        return String.format("%s%s%s",
                spacesConfig.getEndpointUrl(),
                spacesConfig.getBucketName(),
                key);
    }
}
