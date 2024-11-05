package com.undefinedus.backend.controller;

import com.undefinedus.backend.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/member/login")
@Tag(name = "Login", description = "로그인 관련 API")
public class LoginController {
    
    private final MemberService memberService;
    
    @Operation(summary = "사용자 이름 중복 검사", description = "회원가입 시 사용자 이름 중복 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "중복 검사 성공"),
            @ApiResponse(responseCode = "400", description = "중복된 사용자 이름 존재")
    })
    @GetMapping("/username-check")
    public Map<String, String> usernameForDuplicateCheck(
            @Parameter(description = "검사할 사용자 이름", required = true)
            @RequestParam("username") String username) {
        
        try {
            memberService.usernameDuplicateCheck(username);
            return Map.of("result", "success");
        } catch (IllegalArgumentException e) {
            return Map.of("result", "error", "msg", e.getMessage());
        }
    }
    
    @Operation(summary = "닉네임 중복 검사", description = "회원가입 시 닉네임 중복 여부를 확인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "중복 검사 성공"),
            @ApiResponse(responseCode = "400", description = "중복된 닉네임 존재")
    })
    @GetMapping("/nickname-check")
    public Map<String, String> nicknameForDuplicateCheck(
            @Parameter(description = "검사할 닉네임", required = true)
            @RequestParam("nickname") String nickname) {
        try {
            memberService.nicknameDuplicateCheck(nickname);
            return Map.of("result", "success");
        } catch (IllegalArgumentException e) {
            return Map.of("result", "error", "msg", e.getMessage());
        }
    }
}
