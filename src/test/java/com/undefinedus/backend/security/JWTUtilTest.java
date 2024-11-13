package com.undefinedus.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.util.CustomJWTException;
import com.undefinedus.backend.util.JWTUtil;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Log4j2
class JWTUtilTest {
    @Test
    @DisplayName("JWT 토큰 생성 및 검증 테스트")
    void testGenerateAndValidateToken() {
        // given
        MemberSecurityDTO dto = new MemberSecurityDTO(
                "test@test.com",
                "password123",
                "테스터",
                List.of("USER"),
                "일반"
        );
        
        // when
        String accessToken = JWTUtil.generateAccessToken(dto.getClaims());
        Map<String, Object> claims = JWTUtil.validateToken(accessToken);
        
        // then
        assertThat(claims)
                .containsEntry("username", "test@test.com")
                .containsEntry("nickname", "테스터")
                .containsEntry("socialProvider", "일반");
        assertThat((List<String>)claims.get("roles")).contains("USER");
    }
    
    @Test
    @DisplayName("소셜 로그인 사용자 JWT 토큰 생성 및 검증 테스트")
    void testGenerateAndValidateSocialUserToken() {
        // given
        MemberSecurityDTO dto = new MemberSecurityDTO(
                "12345678",
                "password123",
                "카카오테스터",
                List.of("USER"),
                "kakao"
        );
        
        // when
        String accessToken = JWTUtil.generateToken(dto.getClaims(), 30);
        Map<String, Object> claims = JWTUtil.validateToken(accessToken);
        
        // then
        assertAll(
                () -> assertThat(claims)
                        .containsEntry("username", "12345678")
                        .containsEntry("nickname", "카카오테스터")
                        .containsEntry("socialProvider", "kakao"),
                () -> assertThat((List<String>)claims.get("roles")).contains("USER")
        );
    }
    
    @Test
    @DisplayName("관리자 권한 JWT 토큰 생성 및 검증 테스트")
    void testGenerateAndValidateAdminToken() {
        // given
        MemberSecurityDTO dto = new MemberSecurityDTO(
                "admin@test.com",
                "password123",
                "관리자",
                List.of("USER", "ADMIN"),
                "일반"
        );
        
        // when
        String accessToken = JWTUtil.generateToken(dto.getClaims(), 30);
        Map<String, Object> claims = JWTUtil.validateToken(accessToken);
        
        // then
        assertAll(
                () -> assertThat(claims)
                        .containsEntry("username", "admin@test.com")
                        .containsEntry("nickname", "관리자")
                        .containsEntry("socialProvider", "일반"),
                () -> assertThat((List<String>)claims.get("roles"))
                        .contains("USER", "ADMIN")
        );
    }
    
    @Test
    @DisplayName("만료된 토큰 검증 테스트")
    void testExpiredToken() {
        // given
        Map<String, Object> claims = Map.of(
                "username", "test@test.com",
                "nickname", "테스터",
                "roles", List.of("USER"),
                "socialProvider", "일반"
        );
        
        // when & then
        String expiredToken = JWTUtil.generateToken(claims, 0); // 즉시 만료되는 토큰
        CustomJWTException exception = assertThrows(CustomJWTException.class, () -> {
            JWTUtil.validateToken(expiredToken);
        });
        assertEquals("Expired", exception.getMessage());
    }
    
    @Test
    @DisplayName("잘못된 형식의 토큰 검증 테스트")
    void testMalformedToken() {
        // given
        String malformedToken = "invalid.token.format";
        
        // when & then
        CustomJWTException exception = assertThrows(CustomJWTException.class, () -> {
            JWTUtil.validateToken(malformedToken);
        });
        assertEquals("malFormed", exception.getMessage());
    }
    
    @Test
    @DisplayName("토큰 내용 변조 테스트")
    void testTamperedToken() {
        // given
        MemberSecurityDTO dto = new MemberSecurityDTO(
                "test@test.com",
                "password123",
                "테스터",
                List.of("USER"),
                "일반"
        );
        String validToken = JWTUtil.generateToken(dto.getClaims(), 30);
        String tamperedToken = validToken + "tampered";
        
        // when & then
        CustomJWTException exception = assertThrows(CustomJWTException.class, () -> {
            JWTUtil.validateToken(tamperedToken);
        });
        assertEquals("JWTError", exception.getMessage());
    }
}
