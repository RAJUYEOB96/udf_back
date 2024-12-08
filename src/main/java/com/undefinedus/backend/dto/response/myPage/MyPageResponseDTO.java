package com.undefinedus.backend.dto.response.myPage;

import com.undefinedus.backend.domain.entity.SocialLogin;
import com.undefinedus.backend.domain.enums.PreferencesType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
public class MyPageResponseDTO {

    private Long id;

    private String nickname;

    private String profileImage;

    private LocalDate birth;

    private String gender;

    private SocialLogin socialLogin;

    private Set<PreferencesType> preferences;

    private boolean isPublic;

    private boolean isMessageToKakao;

    private boolean KakaoMessageIsAgree;

    private String honorific;
}
