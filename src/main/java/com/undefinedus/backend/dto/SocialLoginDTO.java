package com.undefinedus.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
// 김용
public class SocialLoginDTO {

    private Long id;

    private Long memberId;

    private String provider;

    private String providerId;



}
