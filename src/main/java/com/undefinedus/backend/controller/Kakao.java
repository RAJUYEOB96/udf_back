package com.undefinedus.backend.controller;

import com.undefinedus.backend.service.KakaoTalkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/kakao")
public class Kakao {

    private final KakaoTalkService kakaoTalkService;

    @GetMapping
    public void sendKakao() {

        kakaoTalkService.sendKakaoTalk();
    }
}
