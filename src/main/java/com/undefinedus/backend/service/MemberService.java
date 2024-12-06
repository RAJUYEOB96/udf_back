package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.social.RegisterRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.social.MemberSocialInfoResponseDTO;
import com.undefinedus.backend.dto.response.social.OtherMemberInfoResponseDTO;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

public interface MemberService {

    Map<String, Object> getKakaoInfo(String accessToken, String refreshToken);

    MemberSecurityDTO socialRegister(RegisterRequestDTO requestDTO);

    MemberSecurityDTO regularRegister(RegisterRequestDTO requestDTO);

    void usernameDuplicateCheck(String username);

    void nicknameDuplicateCheck(String nickname);

    // 소셜 로그인 사용자 변환
    default MemberSecurityDTO entityToDTOWithSocial(Member member) {
        return new MemberSecurityDTO(
            member.getUsername(),
            member.getPassword(),
            member.getId(),
            member.getNickname(),
            member.getMemberRoleList().stream()
                .map(memberRole -> memberRole.name())
                .collect(Collectors.toList()),
            member.getSocialLogin().getProvider()  // 소셜 제공자 정보
        );
    }

    // 일반 로그인 사용자 변환
    default MemberSecurityDTO entityToDTOWithRegular(Member member) {
        return new MemberSecurityDTO(
            member.getUsername(),
            member.getPassword(),
            member.getId(),
            member.getNickname(),
            member.getMemberRoleList().stream()
                .map(memberRole -> memberRole.name())
                .collect(Collectors.toList()),
            "일반"  // 일반 로그인 사용자
        );
    }

}
