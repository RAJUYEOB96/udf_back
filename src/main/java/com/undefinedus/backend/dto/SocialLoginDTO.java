package com.undefinedus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SocialLoginDTO {

    private Long id;

    private Long memberId;

    private String provider;

    private String providerId;



}
