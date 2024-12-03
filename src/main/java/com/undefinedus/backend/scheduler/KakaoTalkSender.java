package com.undefinedus.backend.scheduler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class KakaoTalkSender {

    // 나에게 보내기

    private static final String KAKAO_API_URL = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

    public void sendMessage(String accessToken, String message) {
        try {
            HttpURLConnection connection = createConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            connection.setDoOutput(true);

            String payload = "template_object=" + createTemplateJson(message);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                System.out.println("카카오 메시지 전송 성공");
            } else {
                // 실패한 경우 응답 내용 확인
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    System.out.println("카카오 메시지 전송 실패: " + responseCode);
                    System.out.println("응답 내용: " + response.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected HttpURLConnection createConnection() throws Exception {
        URL url = new URL(KAKAO_API_URL);
        return (HttpURLConnection) url.openConnection();
    }

    private String createTemplateJson(String message) {
        // 메시지 텍스트 escaping 처리
        message = message.replace("\"", "\\\"");

        return "{"
            + "\"object_type\": \"text\","
            + "\"text\": \"" + message + "\","
            + "\"link\": {"
            + "\"web_url\": \"https://www.gongchaek.site/\","
            + "\"mobile_web_url\": \"https://www.gongchaek.site/\""
            + "},"
            + "\"button_title\": \"확인\""
            + "}";
    }
}