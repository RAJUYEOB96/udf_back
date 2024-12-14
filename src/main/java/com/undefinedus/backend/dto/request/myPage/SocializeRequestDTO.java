package com.undefinedus.backend.dto.request.myPage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocializeRequestDTO {

    private String kakaoAccessToken;

    private String kakaoRefreshToken;

    private String username;
}
