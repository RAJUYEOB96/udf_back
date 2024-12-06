package com.undefinedus.backend.handler;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

@Log4j2
public class APILoginFailHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
        AuthenticationException exception) throws IOException, ServletException {
        Gson gson = new Gson();

        String jsonStr = gson.toJson(Map.of("error", "ERROR_LOGIN"));

        response.setContentType("application/json");
        // response로부터 출력 스트림을 가져옴 (클라이언트에게 데이터를 보내기 위한 통로)
        PrintWriter printWriter = response.getWriter();
        
        // 생성된 JSON 문자열을 클라이언트에게 출력
        // println()은 문자열 끝에 줄바꿈을 추가
        printWriter.println(jsonStr);
        
        // 출력 스트림을 닫아 리소스 해제
        // 데이터 전송이 완료되었음을 나타냄
        printWriter.close();
    }
}
