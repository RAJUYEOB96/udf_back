package com.undefinedus.backend.service;

public interface MyPageService {

    Boolean checkMessagePermission(Long memberId);

    boolean updateMessageToKakao(Long memberId);
}
