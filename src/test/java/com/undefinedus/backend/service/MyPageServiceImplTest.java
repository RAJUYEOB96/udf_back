package com.undefinedus.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.repository.MemberRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@Transactional
@Log4j2
@ExtendWith(MockitoExtension.class)
public class MyPageServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks // Mock 객체들을 주입받는 객체   // 위에서 생성한 Mock 객체들이 자동으로 주입됨
    private MyPageServiceImpl myPageService;

    private Member mockMember;
    private MultipartFile testFile;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() throws IOException {
        // 테스트용 Member 객체 생성
        mockMember = Member.builder()
            .id(1L)
            .nickname("닉네임")
            .password(passwordEncoder.encode("0000"))
            .profileImage("defaultProfileImage.jpg")
            .birth(LocalDate.parse("2015-01-01"))
            .gender("남")
            .preferences(new HashSet<>())
            .build();

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
    @DisplayName("닉네임, 프로필 이미지 동시 수정 테스트")
    void updateNicknameAndProfile() {
        // given
        String newNickname = "새닉네임";
        given(memberRepository.findById(1L)).willReturn(Optional.of(mockMember));

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
    @DisplayName("취향 수정 테스트")
    void updatePreferences() {
        // given
        List<String> newPreferences = new ArrayList<String>();
    }

    @Test
    @DisplayName("비밀번호 변경 테스트")
    void updatePassword() {

    }
}
