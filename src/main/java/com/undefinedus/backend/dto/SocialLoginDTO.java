package com.undefinedus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SocialLoginDTO {

    private Long id;

    private Long memberId;

    private String provider;

    private String providerId;
    
}
