package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.social.MemberSocialInfoResponseDTO;
import com.undefinedus.backend.dto.response.social.OtherMemberInfoResponseDTO;
import com.undefinedus.backend.service.SocialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    
    // 소셜 메인 (MAIN_0004)에서 닉네임으로 검색했을 때 가져오는 리스트 (팔로잉, 팔로우 전부)
    @GetMapping("/main/search")
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<OtherMemberInfoResponseDTO>>> getOtherMemberList(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @ModelAttribute ScrollRequestDTO requestDTO) {
        
        Long memberId = memberSecurityDTO.getId();
        
        ScrollResponseDTO<OtherMemberInfoResponseDTO> response = socialService.getOtherMembers(memberId, requestDTO);
        
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }
    
    // 팔로우 (SOCIAL_0001) 에서 닉네임으로 검색했을 때 가져오는 리스트 (각각)
    @GetMapping("/follow/search")
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<OtherMemberInfoResponseDTO>>> getFollowMemberList(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @ModelAttribute ScrollRequestDTO requestDTO) {  // tabCondition에 의해 팔로워 팔로잉 리스트 구분되서 가져옴 (없으면 에러)
                                                            // search 가 비어있으면 전체 검색 (tabCondition에 따른)
                                                            // 보낼때 lastId(필요 없지만), lastNickname 둘다 보내기
        Long memberId = memberSecurityDTO.getId();
        
        ScrollResponseDTO<OtherMemberInfoResponseDTO> response = socialService.getFollowMembers(memberId, requestDTO);
        
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }
    
    @PatchMapping("/follow/{targetMemberId}") // 내가 팔로우 상태를 변경시킬 상대의 memberId
    public ResponseEntity<ApiResponseDTO<Void>> toggleFollowStatus(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @PathVariable("targetMemberId") Long targetMemberId) {
        
        Long myMemberId = memberSecurityDTO.getId();
        
        socialService.toggleFollowStatus(myMemberId, targetMemberId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(null));
    }
}
