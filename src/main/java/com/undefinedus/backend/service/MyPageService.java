package com.undefinedus.backend.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import org.springframework.web.multipart.MultipartFile;

public interface MyPageService {

    Boolean checkMessagePermission(Long memberId);

    boolean updateMessageToKakao(Long memberId);

    void updateNicknameAndProfileImage(Long memberId, String nickname, MultipartFile profileImage)
        throws IOException, NoSuchAlgorithmException;
}
