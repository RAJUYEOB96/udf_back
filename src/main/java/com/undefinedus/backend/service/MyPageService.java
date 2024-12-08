package com.undefinedus.backend.service;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.undefinedus.backend.dto.response.myPage.MyPageResponseDTO;
import org.springframework.web.multipart.MultipartFile;

public interface MyPageService {

    Boolean checkMessagePermission(Long memberId);

    boolean updateMessageToKakao(Long memberId);

    Map<String, String> updateNicknameAndProfileImage(Long memberId, String nickname,
        MultipartFile profileImage)
        throws IOException, NoSuchAlgorithmException;

    Map<String, String> dropProfileImage(Long memberId);

    Map<String, String> updateBirthAndGender(Long memberId, LocalDate birth, String gender);

    Map<String, String> updatePreferences(Long memberId, List<String> preferences);

    Map<String, String> updatePassword(Long memberId, String password);

    boolean checkSamePassword(Long memberId, String password);

    MyPageResponseDTO getMyInformation(Long memberId);
}
