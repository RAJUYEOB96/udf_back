package com.undefinedus.backend.service;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.response.myPage.MyPageResponseDTO;
import com.undefinedus.backend.repository.MemberRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.web.multipart.MultipartFile;

//@SpringBootTest
//@Transactional
//@Log4j2
@ExtendWith(MockitoExtension.class)
public class MyPageServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks // Mock 객체들을 주입받는 객체   // 위에서 생성한 Mock 객체들이 자동으로 주입됨
    private MyPageServiceImpl myPageService;

    private Member mockMember;
    private MultipartFile testFile;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트용 Member 객체 생성
        mockMember = Member.builder()
            .id(1L)
            .nickname("닉네임")
            .password("$2a$10$eB7/eUn4FZkRGj9fN4JSeuy4KhUj/UxYrKtJxxgdtrFmMzD2z7TI2")
            .profileImage("defaultProfileImage.jpg")
            .birth(LocalDate.parse("2015-01-01"))
            .gender("남")
            .preferences(new HashSet<>()).isPublic(true)
            .isMessageToKakao(true)
            .KakaoMessageIsAgree(true)
            .isPublic(true)
            .honorific("칭호")
            .createdDate(LocalDateTime.parse("2015-01-01T00:00"))
            .build();

        // Mock 설정
        lenient().when(passwordEncoder.matches("0000", mockMember.getPassword())).thenReturn(true);
        lenient().when(passwordEncoder.matches("wrongPassword", mockMember.getPassword()))
            .thenReturn(false);

        // 테스트용 파일 생성
        Path filePath = Path.of("src/test/resources/test-image.jpg");
        testFile = new MockMultipartFile(
            "file",
            "test-image.jpg",
            "image/jpeg",
            Files.newInputStream(filePath)
        );
    }

    @Test
    @DisplayName("유저 정보 받아오기 테스트")
    void getMyInformation() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));

        // when
        MyPageResponseDTO response = myPageService.getMyInformation(1L);

        // then
        Assertions.assertNotNull(response); // 응답이 null이 아닌지 확인
        assertEquals(1L, response.getId()); // ID 확인
        assertEquals("닉네임", response.getNickname()); // 닉네임 확인
        assertEquals("defaultProfileImage.jpg", response.getProfileImage()); // 프로필 이미지 확인
        assertEquals(LocalDate.parse("2015-01-01"), response.getBirth()); // 생년월일 확인
        assertEquals("남", response.getGender()); // 성별 확인
        assertTrue(response.isPublic()); // 공개 여부 확인
        assertTrue(response.isMessageToKakao()); // 카카오 메시지 설정 확인
        assertTrue(response.isKakaoMessageIsAgree()); // 카카오 메시지 동의 확인
        assertEquals("칭호", response.getHonorific()); // 칭호 확인
        assertEquals(new HashSet<>(), response.getPreferences()); // 취향 확인
        assertEquals(LocalDateTime.parse("2015-01-01T00:00"), response.getCreatedDate());

        // Mock 객체 호출 검증
        verify(memberRepository).findById(1L); // findById 메서드가 호출되었는지 확인
    }

    @Test
    @DisplayName("책장 공개 여부 설정 테스트")
    void updateIsPublic() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));

        // when
        boolean result = myPageService.updateIsPublic(1L);

        // then
        assertFalse(result);
        // Mock 객체 호출 검증
        verify(memberRepository).findById(1L); // findById 메서드가 호출되었는지 확인
    }

    @Test
    @DisplayName("닉네임 수정 테스트")
    void updateOnlyNickname() throws IOException, NoSuchAlgorithmException {
        // given
        String newNickname = "새닉네임";
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));

        // when
        Map<String, String> result = myPageService.updateNicknameAndProfileImage(1L, newNickname,
            null);

        // then
        assertEquals("success", result.get("nickname"));
        assertNull(result.get("profileImage"));
        assertEquals(newNickname, mockMember.getNickname());
        verify(memberRepository).findById(1L); // findById 호출 여부 확인
    }

    @Test
    @DisplayName("프로필이미지 수정 테스트")
    @Commit
    void updateOnlyProfileImage() throws IOException, NoSuchAlgorithmException {
        // given
        String uniqueFileName = UUID.randomUUID() + "-" + testFile.getOriginalFilename();
        String mockUrl = "https://mock-s3-url/" + uniqueFileName;
        given(s3Service.uploadFile(uniqueFileName, testFile)).willReturn(mockUrl);

        // when
        String uploadedFileUrl = s3Service.uploadFile(uniqueFileName, testFile);

        // then
        assertEquals(mockUrl, uploadedFileUrl);
        verify(s3Service).uploadFile(uniqueFileName, testFile); // uploadFile 호출 여부 확인
    }

    @Test
    @DisplayName("프로필사진 삭제 테스트")
    void dropProfileImage() {
        // given
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));

        // when
        Map<String, String> result = myPageService.dropProfileImage(1L);

        // then
        assertEquals("success", result.get("dropProfileImage"));
        verify(memberRepository).findById(1L); // findById 호출 여부 확인
    }

    @Test
    @DisplayName("닉네임, 프로필 이미지 동시 수정 테스트")
    void updateNicknameAndProfile() throws IOException, NoSuchAlgorithmException {
        // given
        String newNickname = "새닉네임";
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        String uniqueFileName = UUID.randomUUID() + "-" + testFile.getOriginalFilename();
        String mockUrl = "https://mock-s3-url/" + uniqueFileName;
        given(s3Service.uploadFile(uniqueFileName, testFile)).willReturn(mockUrl);

        // when
        Map<String, String> result = myPageService.updateNicknameAndProfileImage(1L, newNickname,
            null);
        String uploadedFileUrl = s3Service.uploadFile(uniqueFileName, testFile);

        // then
        assertEquals("success", result.get("nickname"));
        assertNull(result.get("profileImage"));
        assertEquals(newNickname, mockMember.getNickname());
        verify(memberRepository).findById(1L); // findById 호출 여부 확인
        assertEquals(mockUrl, uploadedFileUrl);
        verify(s3Service).uploadFile(uniqueFileName, testFile); // uploadFile 호출 여부 확인

    }

    @Test
    @DisplayName("출생연도 수정 테스트")
    void updateOnlyBirth() {
        // given
        LocalDate newBirth = LocalDate.of(1996, 6, 8);
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));

        // when
        Map<String, String> result = myPageService.updateBirthAndGender(1L, newBirth, null);

        // then
        assertEquals("success", result.get("birth"));
        assertNull(result.get("gender"));
        assertEquals(newBirth, mockMember.getBirth());
        verify(memberRepository).findById(1L); // findById 호출 여부 확인
    }

    @Test
    @DisplayName("성별 수정 테스트")
    void updateOnlyGender() {
        // given
        String newGender = "여";
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));

        // when
        Map<String, String> result = myPageService.updateBirthAndGender(1L, null, newGender);

        // then
        assertEquals("success", result.get("gender"));
        assertNull(result.get("birth"));
        assertEquals(newGender, mockMember.getGender());
        verify(memberRepository).findById(1L); // findById 호출 여부 확인
    }

    @Test
    @DisplayName("출생연도, 성별 동시 수정 테스트")
    void updateBirthAndGender() {
        // given
        LocalDate newBirth = LocalDate.of(1996, 6, 8);
        String newGender = "여";
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));

        // when
        Map<String, String> result = myPageService.updateBirthAndGender(1L, newBirth, newGender);

        // then
        assertEquals("success", result.get("gender"));
        assertEquals("success", result.get("birth"));
        assertEquals(newGender, mockMember.getGender());
        assertEquals(newBirth, mockMember.getBirth());
        verify(memberRepository).findById(1L); // findById 호출 여부 확인
    }

    @Test
    @DisplayName("취향 수정 성공 테스트")
    void updatePreferences_success() {
        // given
        List<String> newPreferences = List.of("사회과학", "만화", "과학");
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));

        // when
        Map<String, String> result = myPageService.updatePreferences(1L, newPreferences);

        // then
        assertEquals("success", result.get("preferences"));
        verify(memberRepository).findById(1L); // findById 호출 여부 확인
    }

    @Test
    @DisplayName("취향 수정 실패 테스트")
    void updatePreferences_fail() {
        // given
        List<String> newPreferences = List.of("사회학", "화", "과");
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));

        // when
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            myPageService.updatePreferences(1L, newPreferences);
        });

        // then
        assertEquals("잘못된 취향 값: 사회학", exception.getMessage());
        verify(memberRepository).findById(1L); // findById 호출 여부 확인
    }

    @Test
    @DisplayName("기존 비밀번호와 동일한 경우 테스트")
    void checkPassword_same() {
        // given
        String prevPassword = "0000";
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        given(passwordEncoder.matches("0000", mockMember.getPassword())).willReturn(
            true); // 올바른 비밀번호

        // when
        boolean result = myPageService.checkSamePassword(1L, prevPassword);

        // then
        assertTrue(result);
        verify(memberRepository).findById(1L); // findById 호출 여부 확인
    }

    @Test
    @DisplayName("기존 비밀번호와 다른 경우 테스트")
    void checkPassword_different() {
        // given
        String prevPassword = "sd";
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        given(passwordEncoder.matches("sd", mockMember.getPassword())).willReturn(
            false); // 올바른 비밀번호

        // when
        boolean result = myPageService.checkSamePassword(1L, prevPassword);

        // then
        assertFalse(result);
        verify(memberRepository).findById(1L); // findById 호출 여부 확인
    }

    @Test
    @DisplayName("비밀번호 변경 성공 테스트")
    void updatePassword_success() {
        // given
        String newPassword = "1111";
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        given(passwordEncoder.matches("1111", mockMember.getPassword())).willReturn(
            false); // 올바른 비밀번호

        // when
        Map<String, String> result = myPageService.updatePassword(1L, newPassword);

        // then
        assertEquals("success", result.get("password"));
        verify(memberRepository, times(2)).findById(1L); // findById 호출 여부 확인
    }

    @Test
    @DisplayName("비밀번호 변경 실패 테스트")
    void updatePassword_fail() {
        // given
        String newPassword = "0000";
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));
        given(passwordEncoder.matches("0000", mockMember.getPassword())).willReturn(
            true); // 올바른 비밀번호

        // when
        Map<String, String> result = myPageService.updatePassword(1L, newPassword);

        // then
        assertEquals("fail", result.get("password"));
        verify(memberRepository, times(2)).findById(1L); // findById 호출 여부 확인
    }
}
