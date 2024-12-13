package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.request.myPage.SocializeRequestDTO;
import com.undefinedus.backend.dto.response.myPage.MyPageResponseDTO;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface MyPageService {

    Member socializeMember(Long memberId, SocializeRequestDTO socializeRequestDTO);

    Boolean checkMessagePermission(Long memberId);

    Boolean updateMessagePermission(Long memberId);

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

    boolean updateIsPublic(Long memberId);
}
