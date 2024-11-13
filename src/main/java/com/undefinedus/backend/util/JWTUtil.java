package com.undefinedus.backend.util;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.InvalidClaimException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;

public class JWTUtil {
    private static String key = "C3D55646D69EFB646A3C5B316ED4E66793666D7AD1E85FE121B28D5575";
    
    // Access Token 유효시간 (30분)
    private static final int ACCESS_TOKEN_EXPIRE_MINUTES = 30;
    
    // Refresh Token 유효시간 (28일 - 4주)
    private static final int REFRESH_TOKEN_EXPIRE_MINUTES = 60 * 24 * 7 * 4;
    
    // Access Token 생성
    public static String generateAccessToken(Map<String, Object> valueMap) {
        return generateToken(valueMap, ACCESS_TOKEN_EXPIRE_MINUTES);
    }
    
    // Refresh Token 생성
    public static String generateRefreshToken(Map<String, Object> valueMap) {
        return generateToken(valueMap, REFRESH_TOKEN_EXPIRE_MINUTES);
    }
    
    // 실제 토큰 생성 메서드
    public static String generateToken(Map<String, Object> valueMap, int min) {
        SecretKey key = null;
        
        try {
            key = Keys.hmacShaKeyFor(JWTUtil.key.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        
        return Jwts.builder()
                .setHeader(Map.of("typ", "JWT"))
                .setClaims(valueMap)
                .setIssuedAt(Date.from(ZonedDateTime.now().toInstant()))
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(min).toInstant()))
                .signWith(key)
                .compact();
    }
    
    public static Map<String, Object> validateToken(String token) {
        Map<String, Object> claim = null;

        try {
            SecretKey key = Keys.hmacShaKeyFor(JWTUtil.key.getBytes("UTF-8"));

            claim = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token) // 파싱 및 검증. 실패 시 에러
                .getBody();

        } catch (MalformedJwtException malformedJwtException) {
            throw new CustomJWTException("malFormed");
        } catch (ExpiredJwtException expiredJwtException) {
            throw new CustomJWTException("Expired");
        } catch (InvalidClaimException invalidClaimException) {
            throw new CustomJWTException("Invalid");
        } catch (JwtException jwtException) {
            throw new CustomJWTException("JWTError");
        } catch (Exception e) {
            throw new CustomJWTException("Error");
        }
        return claim;
    }
}
