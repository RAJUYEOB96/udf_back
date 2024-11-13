package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.social.RegisterRequestDTO;
import com.undefinedus.backend.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/member")
@Tag(name = "Social Login", description = "소셜 로그인 관련 API")
public class SocialController {
    
    private final MemberService memberService;
    
    @Operation(
            summary = "카카오 회원 정보 조회",
            description = "카카오 액세스 토큰을 사용하여 회원 정보를 조회합니다. 기존 회원인 경우 회원 정보를, 신규 회원인 경우 카카오 정보를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "카카오 회원 정보 조회 성공 (기존 회원)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"result\": \"exists\", \"member\": {\"id\": 1, \"username\": \"kakao_12345\", \"nickname\": \"사용자\"}}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "200",
                    description = "카카오 회원 정보 조회 성공 (신규 회원)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"result\": \"new\", \"kakaoId\": \"12345\", \"nickname\": \"사용자\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 액세스 토큰",
                    content = @Content(
                            schema = @Schema(
                                    type = "object",
                                    example = "{\"error\": \"Invalid access token\"}"
                            )
                    )
            )
    })
    @GetMapping("/kakao")
    public Map<String, Object> getMemberFromKakao(
            @Parameter(description = "카카오 액세스 토큰", required = true)
            String accessToken) {
        
        log.info("Kakao accessToken : " + accessToken);
        
        return memberService.getKakaoInfo(accessToken);
        
    }
    
    // /api/member/kakao 에서 리턴받은 값을 각각 username, nickname 변수에 담아서 보내기
    // kakaoId -> username
    // nickname -> nickname
    @Operation(
            summary = "소셜 회원가입",
            description = "카카오 회원 정보를 기반으로 서비스 회원가입을 진행합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = """
                                {
                                    "result": "success",
                                    "member": {
                                        "username": "kakao_12345",
                                        "password": "[PROTECTED]",
                                        "nickname": "사용자닉네임",
                                        "socialLoginDTO": {
                                            "provider": "KAKAO",
                                            "providerId": "12345"
                                        },
                                        "memberRoleList": ["USER"]
                                    }
                                }
                                """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "회원가입 실패",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "object",
                                    example = """
                                {
                                    "result": "error",
                                    "msg": "취향이 선택되어지지 않았습니다."
                                }
                                """
                            )
                    )
            )
    })
    @PostMapping("/social-register")
    public Map<String, Object> kakaoSocialRegister(
            @Parameter(description = "회원가입 요청 정보", required = true)
            @RequestBody RegisterRequestDTO requestDTO) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            MemberSecurityDTO memberSecurityDTO = memberService.socialRegister(requestDTO);
            result.put("member", memberSecurityDTO);
            result.put("result", "success");
            return result;
        } catch (Exception e) {
            log.error("socialRegister Error : ", e);
            return Map.of("result", "error", "msg", e.getMessage());
        }
    }
}
