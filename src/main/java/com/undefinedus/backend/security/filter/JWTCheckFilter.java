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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

// 김용
public class JWTCheckFilter extends OncePerRequestFilter {

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        String path = request.getRequestURI();
        if (path.contains("/api/member/login")) {
            return true;
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {


        String authHeaderStr = request.getHeader("Authorization");

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

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(memberDTO, password, memberDTO.getAuthorities());

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
