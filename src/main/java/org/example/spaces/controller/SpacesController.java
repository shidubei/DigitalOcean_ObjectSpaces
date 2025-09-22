package org.example.spaces.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.spaces.model.ApiResponse;
import org.example.spaces.model.FileMetadata;
import org.example.spaces.service.SpacesService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/spaces")
@RequiredArgsConstructor
@Tag(name= "Spaces API", description = "DigitalOcean Spaces 文件管理API")

public class SpacesController {
    private final SpacesService spacesService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    @Operation(summary = "上传文件", description = "上传文件到DigitalOcean Spaces")
    public ResponseEntity<ApiResponse<FileMetadata>> uploadFile(
            @Parameter(description = "要上传的文件", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "目标文件夹（可选）")
            @RequestParam(value = "folder", required = false) String folder) {

        log.info("接收到文件上传请求: {}, 大小: {} bytes",
                file.getOriginalFilename(), file.getSize());

        FileMetadata metadata = spacesService.uploadFile(file, folder);
        return ResponseEntity.ok(
                ApiResponse.success("文件上传成功", metadata)
        );
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{key}")
    @Operation(summary = "下载文件", description = "从DigitalOcean Spaces下载文件")
    public void downloadFile(
            @Parameter(description = "文件键", required = true)
            @PathVariable String key,
            HttpServletResponse response) throws IOException {

        log.info("接收到文件下载请求: {}", key);

        // 获取文件元数据
        FileMetadata metadata = spacesService.getFileMetadata(key);

        // 设置响应头
        response.setContentType(metadata.getContentType() != null
                ? metadata.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setContentLengthLong(metadata.getSize());

        // 设置下载文件名
        String fileName = key.substring(key.lastIndexOf("/") + 1);
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename*=UTF-8''" + encodedFileName);

        // 写入文件内容
        try (InputStream inputStream = spacesService.downloadFile(key)) {
            inputStream.transferTo(response.getOutputStream());
            response.flushBuffer();
        }
    }

    /**
     * 获取文件元数据
     */
    @GetMapping("/metadata/{key}")
    @Operation(summary = "获取文件元数据", description = "获取指定文件的元数据信息")
    public ResponseEntity<ApiResponse<FileMetadata>> getFileMetadata(
            @Parameter(description = "文件键", required = true)
            @PathVariable String key) {

        log.info("获取文件元数据: {}", key);

        FileMetadata metadata = spacesService.getFileMetadata(key);
        return ResponseEntity.ok(
                ApiResponse.success("获取文件元数据成功", metadata)
        );
    }

    /**
     * 列出文件
     */
    @GetMapping("/list")
    @Operation(summary = "列出文件", description = "列出存储桶中的所有文件")
    public ResponseEntity<ApiResponse<List<FileMetadata>>> listFiles(
            @Parameter(description = "前缀过滤（可选）")
            @RequestParam(value = "prefix", required = false) String prefix) {

        log.info("列出文件，前缀: {}", prefix);

        List<FileMetadata> files = spacesService.listFiles(prefix);
        return ResponseEntity.ok(
                ApiResponse.success(
                        String.format("找到 %d 个文件", files.size()),
                        files
                )
        );
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{key}")
    @Operation(summary = "删除文件", description = "删除指定的文件")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @Parameter(description = "文件键", required = true)
            @PathVariable String key) {

        log.info("删除文件: {}", key);

        boolean success = spacesService.deleteFile(key);
        if (success) {
            return ResponseEntity.ok(
                    ApiResponse.success("文件删除成功", null)
            );
        } else {
            return ResponseEntity.ok(
                    ApiResponse.error("文件删除失败")
            );
        }
    }

    /**
     * 检查文件是否存在
     */
    @GetMapping("/exists/{key}")
    @Operation(summary = "检查文件存在性", description = "检查指定文件是否存在")
    public ResponseEntity<ApiResponse<Boolean>> fileExists(
            @Parameter(description = "文件键", required = true)
            @PathVariable String key) {

        log.info("检查文件是否存在: {}", key);

        boolean exists = spacesService.fileExists(key);
        return ResponseEntity.ok(
                ApiResponse.success(
                        exists ? "文件存在" : "文件不存在",
                        exists
                )
        );
    }
}
