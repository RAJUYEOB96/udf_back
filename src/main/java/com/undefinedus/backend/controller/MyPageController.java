package com.undefinedus.backend.controller;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.myPage.PasswordRequestDTO;
import com.undefinedus.backend.dto.request.myPage.SocializeRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.myPage.MyPageResponseDTO;
import com.undefinedus.backend.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@Log4j2
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(description = "일반회원 -> 카카오 회원 전환")
    @PatchMapping("/socialize")
    public Map<String, Object> socializeMember(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestBody SocializeRequestDTO socializeRequestDTO
    ) {
        Map<String, Object> result = new HashMap<>();

        Long memberId = memberSecurityDTO.getId();
        try {
            Member updatedMember = myPageService.socializeMember(memberId, socializeRequestDTO);

            result.put("member", updatedMember);
            result.put("result", "success");
            return result;
        } catch (Exception e) {
            log.error("socialRegister Error : ", e);
            return Map.of("result", "error", "msg", e.getMessage());
        }
    }

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

    @Operation(description = "카카오 메시지 권한 수정")
    @PostMapping("/kakao/message")
    public ResponseEntity<ApiResponseDTO<Boolean>> updateProcessKakaoAgree(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO
    ) {
        Long memberId = memberSecurityDTO.getId();
        boolean result = myPageService.updateMessagePermission(memberId);

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

    @Operation(description = "책장 공개 여부 설정")
    @PostMapping("/public")
    public ResponseEntity<ApiResponseDTO<Boolean>> updateIsPublic(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO
    ) {
        Long memberId = memberSecurityDTO.getId();
        boolean result = myPageService.updateIsPublic(memberId);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(result));
    }

    @Operation(description = "사용자 정보 데이터")
    @GetMapping
    public ResponseEntity<ApiResponseDTO<MyPageResponseDTO>> getMyInformation(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO
    ) {
        Long memberId = memberSecurityDTO.getId();

        MyPageResponseDTO response = myPageService.getMyInformation(memberId);

        return ResponseEntity.ok(ApiResponseDTO.success(response));
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
