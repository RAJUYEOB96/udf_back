package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.service.MemberService;
import com.undefinedus.backend.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/myPage")
public class MyPageController {

    private final MyPageService myPageService;
    private final MemberService memberService;

    @Operation(description = "카카오 메시지 권한 확인")
    @GetMapping("/kakao/message")
    public ResponseEntity<ApiResponseDTO<Boolean>> checkProcessKakaoMessage(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO
    ) {
        Long memberId = memberSecurityDTO.getId();
        boolean result = myPageService.checkMessagePermission(memberId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(result));
    }

    @Operation(description = "카카오 메시지 받기 허용, 비허용")
    @PostMapping("/kakao/update")
    public ResponseEntity<ApiResponseDTO<Boolean>> updateProcessKakaoMessage(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO
    ) {
        Long memberId = memberSecurityDTO.getId();
        boolean result = myPageService.updateMessageToKakao(memberId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(result));
    }

    @PatchMapping("/profile")
    public Map<String, String> updateProfile(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
        @RequestPart(value = "nickname", required = false) String nickname
    ) throws IOException, NoSuchAlgorithmException {
        Long memberId = memberSecurityDTO.getId();

        memberService.updateNicknameAndProfileImage(memberId, nickname, profileImage);

        return Map.of();
    }
}
