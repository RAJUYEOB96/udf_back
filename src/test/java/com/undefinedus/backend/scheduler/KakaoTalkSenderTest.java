package com.undefinedus.backend.scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.OutputStream;
import java.net.HttpURLConnection;

import static org.mockito.Mockito.*;

class KakaoTalkSenderTest {

    private KakaoTalkSender kakaoTalkSender;

    @Mock
    private HttpURLConnection mockConnection;

    @Mock
    private OutputStream mockOutputStream;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Spy로 KakaoTalkSender 생성
        kakaoTalkSender = spy(new KakaoTalkSender());

        // createConnection 메서드를 Mock 처리
        doReturn(mockConnection).when(kakaoTalkSender).createConnection();

        // Mock 설정
        when(mockConnection.getOutputStream()).thenReturn(mockOutputStream);
        when(mockConnection.getResponseCode()).thenReturn(200);
    }

    @Test
    void testSendMessage_Success() throws Exception {
        // Given
        String accessToken = "test-access-token";
        String message = "Hello, Kakao!";

        // When
        kakaoTalkSender.sendMessage(accessToken, message);

        // Then
        verify(mockConnection).setRequestMethod("POST");
        verify(mockConnection).setRequestProperty("Authorization", "Bearer " + accessToken);
        verify(mockOutputStream).write(any(byte[].class));
        verify(mockConnection).getResponseCode();
    }

    @Test
    void testSendMessage_Failure() throws Exception {
        // Given
        when(mockConnection.getResponseCode()).thenReturn(400);

        String accessToken = "invalid-access-token";
        String message = "Failed Message";

        // When
        kakaoTalkSender.sendMessage(accessToken, message);

        // Then
        verify(mockConnection).getResponseCode();
    }
}