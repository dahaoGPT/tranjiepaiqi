package com.jiepaiqi.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理非法参数异常。
     * 返回400 Bad Request状态码和错误消息。
     * 
     * @param ex 异常实例
     * @return 包含错误消息的响应实体
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ErrorResponse.builder().message(ex.getMessage()).build());
    }

    /**
     * 处理非法状态异常。
     * 返回409 Conflict状态码和错误消息。
     * 
     * @param ex 异常实例
     * @return 包含错误消息的响应实体
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder().message(ex.getMessage()).build());
    }

    /**
     * 处理文件上传大小超限异常。
     * 返回413 Payload Too Large状态码和错误消息。
     * 
     * @param ex 异常实例
     * @return 包含错误消息的响应实体
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(ErrorResponse.builder().message("文件大小超过限制").build());
    }

    /**
     * 处理运行时异常。
     * 返回500 Internal Server Error状态码和错误消息。
     * 
     * @param ex 异常实例
     * @return 包含错误消息的响应实体
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.builder().message(ex.getMessage()).build());
    }

    @lombok.Data
    @lombok.Builder
    public static class ErrorResponse {
        private String message;
    }
}