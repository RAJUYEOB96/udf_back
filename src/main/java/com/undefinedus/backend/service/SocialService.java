package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.response.social.MemberSocialInfoResponseDTO;

public interface SocialService {
    
    MemberSocialInfoResponseDTO getMemberSocialSimpleInfo(Long memberId);
}
