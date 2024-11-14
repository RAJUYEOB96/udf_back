package com.undefinedus.backend.handler;

import com.google.gson.Gson;
import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.util.JWTUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Log4j2
public class APILoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) throws IOException, ServletException {

        MemberSecurityDTO memberSecurityDTO = (MemberSecurityDTO) authentication.getPrincipal();
        
        System.out.println("memberSecurityDTO = " + memberSecurityDTO);

        Map<String, Object> claims = memberSecurityDTO.getClaims();

        String accessToken = JWTUtil.generateAccessToken(claims);
        String refreshToken = JWTUtil.generateRefreshToken(claims);

        claims.put("accessToken", accessToken);
        claims.put("refreshToken", refreshToken);

        Gson gson = new Gson();

        String jsonStr = gson.toJson(claims);
        
        System.out.println("jsonStr = " + jsonStr);
        
        response.setContentType("application/json; charset=UTF-8");

        PrintWriter printWriter = response.getWriter();

        printWriter.println(jsonStr);
        printWriter.close();
    }
}
