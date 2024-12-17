package com.undefinedus.backend.dto.request.member;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordUpdateRequestDTO {
    
    private String email;
    
    private String newPassword;
}
