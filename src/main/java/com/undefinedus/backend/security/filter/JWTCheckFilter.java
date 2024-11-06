package com.undefinedus.backend.security.filter;

import com.google.gson.Gson;
import com.undefinedus.backend.dto.MemberDTO;
import com.undefinedus.backend.dto.SocialLoginDTO;
import com.undefinedus.backend.util.JWTUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
public class JWTCheckFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String path = request.getRequestURI();

        return path.contains("/api/member/login") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/swagger-resources") ||
                path.startsWith("/webjars/") ||
                path.contains("/api/member/register") ||
                path.contains("/api/member/test") ||
                path.contains("/api/member/refresh") ||
                path.contains("/api/member/email/send-verification") ||  // 이메일 인증 발송
                path.contains("/api/member/email/verify");              // 이메일 인증 확인
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeaderStr = request.getHeader("Authorization");

        // JWT 토큰이 없는 경우 바로 다음 필터로 진행
        if(authHeaderStr == null || !authHeaderStr.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String accessToken = authHeaderStr.substring(7);
            Map<String, Object> claims = JWTUtil.validateToken(accessToken);

            String username = (String) claims.get("username");
            String password = (String) claims.get("password");
            String nickname = (String) claims.get("nickname");
            List<String> memberRoleList = (List<String>) claims.get("memberRoleList");

            SocialLoginDTO socialLoginDTO = null;
            if (claims.get("socialLoginDTO") != null) {
                Map<String, Object> socialLoginClaims = (Map<String, Object>) claims.get("socialLoginDTO");
                socialLoginDTO = new Gson().fromJson(new Gson().toJson(socialLoginClaims), SocialLoginDTO.class);
            }

            MemberDTO memberDTO = new MemberDTO(username, password, nickname, socialLoginDTO, memberRoleList);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberDTO,
                    password, memberDTO.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);

            filterChain.doFilter(request, response);

        } catch (Exception e) {

            Gson gson = new Gson();

            String msg = gson.toJson(Map.of(
                    "error", "ERROR_ACCESS_TOKEN",
                    "message", e.getMessage(),
                    "cause", e.getCause() != null ? e.getCause().toString() : "No cause available"
            ));

            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            PrintWriter printWriter = response.getWriter();
            printWriter.println(msg);
            printWriter.close();
        }

    }
}
