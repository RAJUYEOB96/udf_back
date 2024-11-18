package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.social.MemberSocialInfoResponseDTO;
import com.undefinedus.backend.service.MemberService;
import com.undefinedus.backend.service.SocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/social")
public class SocialController {
    // TODO: 프로필 이미지에 대한 것들은 나중에 추가하기
    private final SocialService socialService;
    
    // 소셜 메인 (MAIN_0004), 팔로잉 팔로워(SOCIAL_0001) 목록에서 나의 정보 가져가기
    @GetMapping("/myInfo")
    public ResponseEntity<ApiResponseDTO<MemberSocialInfoResponseDTO>> getMySimpleSocialInfo(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO) {
        
        Long memberId = memberSecurityDTO.getId();
        
        MemberSocialInfoResponseDTO response = socialService.getMemberSocialSimpleInfo(memberId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }
    
    
}
