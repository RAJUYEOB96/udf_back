package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.social.MemberSocialInfoResponseDTO;
import com.undefinedus.backend.dto.response.social.OtherMemberInfoResponseDTO;

public interface SocialService {

    MemberSocialInfoResponseDTO getMemberSocialSimpleInfo(Long memberId);

    ScrollResponseDTO<OtherMemberInfoResponseDTO> getOtherMembers(Long memberId,
        ScrollRequestDTO requestDTO);

    ScrollResponseDTO<OtherMemberInfoResponseDTO> getMemberFollows(Long memberId,
        ScrollRequestDTO requestDTO);

    void toggleFollowStatus(Long myMemberId, Long targetMemberId);

    MemberSocialInfoResponseDTO getOtherMemberSocialSimpleInfo(Long myMemberId,
        Long targetMemberId);

    ScrollResponseDTO<OtherMemberInfoResponseDTO> getOtherMemberFollows(Long memberId,
        Long targetMemberId, ScrollRequestDTO requestDTO);
}
