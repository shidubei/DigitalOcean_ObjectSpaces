package org.example.spaces.exception;

import lombok.extern.slf4j.Slf4j;
import org.example.spaces.model.ApiResponse;
import org.springdoc.webmvc.ui.SwaggerResourceResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/*
* 全局异常处理器
* 统一处理应用程序中的异常
* */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private final SwaggerResourceResolver swaggerResourceResolver;

    public GlobalExceptionHandler(SwaggerResourceResolver swaggerResourceResolver) {
        this.swaggerResourceResolver = swaggerResourceResolver;
    }

    // 处理文件存储异常
    @ExceptionHandler(FileStorageException.class)
    public ResponseEntity<ApiResponse<Void>> handleFileStorageException(FileStorageException ex) {
        log.error("文件处理异常 {}",ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ex.getMessage()));
    }

    // 处理文件大小超出限制响应
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        log.error("文件大小超出限制 {}",ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error("文件大小超出限制"));
    }

    // 处理其他未知异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("未知异常 {}",ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error("服务器内部错误"));
    }
}
