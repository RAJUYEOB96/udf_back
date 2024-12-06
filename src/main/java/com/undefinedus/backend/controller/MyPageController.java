package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.myPage.PasswordRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.service.MemberService;
import com.undefinedus.backend.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Operation(description = "프로필사진 삭제")
    @PostMapping("/profile/drop")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> dropProfileImage(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO
    ) {
        Long memberId = memberSecurityDTO.getId();

        Map<String, String> result = myPageService.dropProfileImage(memberId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(result));
    }

    @Operation(description = "프로필 사진, 닉네임 수정")
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> updateProfile(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
        @RequestPart(value = "nickname", required = false) String nickname
    ) throws IOException, NoSuchAlgorithmException {
        Long memberId = memberSecurityDTO.getId();

        Map<String, String> result = myPageService.updateNicknameAndProfileImage(memberId, nickname,
            profileImage);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(result));
    }

    @Operation(description = "생년월일, 성별 수정")
    @PatchMapping("/userInfo")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> updateUserInfo(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestParam(value = "birth", required = false) LocalDate birth,
        @RequestParam(value = "gender", required = false) String gender) {
        Long memberId = memberSecurityDTO.getId();

        Map<String, String> result = myPageService.updateBirthAndGender(memberId, birth, gender);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(result));
    }

    @Operation(description = "취향 수정")
    @PatchMapping("/preferences")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> updatePreferences(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestBody List<String> preferences
    ) {
        Long memberId = memberSecurityDTO.getId();

        Map<String, String> result = myPageService.updatePreferences(memberId, preferences);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(result));
    }

    @Operation(description = "기존 비밀번호와 같은 지 체킹")
    @PostMapping("/checkPassword")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> checkSamePassword(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestBody PasswordRequestDTO passwordRequestDTO
    ) {
        Long memberId = memberSecurityDTO.getId();

        boolean isSame = myPageService.checkSamePassword(memberId,
            passwordRequestDTO.getPrevPassword());

        Map<String, String> result = new HashMap<String, String>();

        if (isSame) {
            result.put("password", "same");
        } else {
            result.put("password", "different");
        }
        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(result));
    }

    @Operation(description = "비밀번호 변경")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponseDTO<Map<String, String>>> updatePassword(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestBody PasswordRequestDTO passwordRequestDTO
    ) {
        Long memberId = memberSecurityDTO.getId();

        Map<String, String> result = myPageService.updatePassword(memberId,
            passwordRequestDTO.getNewPassword());

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(result));
    }
}
