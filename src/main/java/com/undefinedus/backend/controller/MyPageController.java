package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/myPage")
public class MyPageController {

    private final MyPageService myPageService;

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
}
