package com.undefinedus.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponseDTO<T> {
    private String result;
    private String message;
    private T data;
    
    public static <T> ApiResponseDTO<T> success(T data) {
        return new ApiResponseDTO<>("success", null, data);
    }
    
    public static <T> ApiResponseDTO<T> error(String message) {
        return new ApiResponseDTO<>("error", message, null);
    }
}
