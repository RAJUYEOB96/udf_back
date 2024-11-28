package com.undefinedus.backend.scheduler;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class KakaoTalkSender {

    // todo: 카카오톡
    private static final String KAKAO_API_URL = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

    public void sendMessage(String accessToken, String message) {
        try {
            URL url = new URL(KAKAO_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            connection.setDoOutput(true);

            String payload = "template_object=" + createTemplateJson(message);
            try (OutputStream os = connection.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                System.out.println("카카오 메시지 전송 성공");
            } else {
                System.out.println("카카오 메시지 전송 실패: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createTemplateJson(String message) {
        return "{"
            + "\"object_type\": \"text\","
            + "\"text\": \"" + message + "\","
            + "\"link\": {"
            + "\"web_url\": \"https://www.example.com\","
            + "\"mobile_web_url\": \"https://m.example.com\""
            + "},"
            + "\"button_title\": \"확인\""
            + "}";
    }
}