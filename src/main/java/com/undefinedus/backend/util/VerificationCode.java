package com.undefinedus.backend.util;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerificationCode {
    private String code;
    private LocalDateTime expiryTime;
    
    // true 이면 만료
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
