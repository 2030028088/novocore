package com.aurora.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private Integer code;
    private String message;
    private T data;
    private String timestamp;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, "Success", data, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, 200, message, data, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, 200, "Success", null, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(false, code, message, null, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, 500, message, null, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(false, 400, message, null, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(false, 401, message, null, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(false, 403, message, null, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(false, 404, message, null, LocalDateTime.now().toString());
    }

    public static <T> ApiResponse<T> tooManyRequests(String message) {
        return new ApiResponse<>(false, 429, message, null, LocalDateTime.now().toString());
    }
}