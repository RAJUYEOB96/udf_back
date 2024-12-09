package com.undefinedus.backend.dto.response.myPage;

import com.undefinedus.backend.domain.entity.SocialLogin;
import com.undefinedus.backend.domain.enums.PreferencesType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyPageResponseDTO {

    private Long id;

    private String nickname;

    private String profileImage;

    private LocalDate birth;

    private String gender;

    private boolean isSocial;

    private Set<PreferencesType> preferences;

    private boolean isPublic;

    private boolean isMessageToKakao;

    private boolean KakaoMessageIsAgree;

    private String honorific;

    private LocalDateTime createdDate;
}
