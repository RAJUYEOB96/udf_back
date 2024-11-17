package com.undefinedus.backend.exception.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    private final String message;
    private final String errorCode;  // 필요한 경우 추가
    
    public ErrorResponse(String message) {
        this.message = message;
        this.errorCode = null;
    }
}