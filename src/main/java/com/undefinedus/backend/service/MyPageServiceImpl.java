package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.dto.response.myPage.MyPageResponseDTO;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;


@RequiredArgsConstructor
@Service
@Log4j2
@Transactional
public class MyPageServiceImpl implements MyPageService {

    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final PasswordEncoder passwordEncoder;

    // 카카오 회원이 카카오톡 메시지 권한을 허용했는지 체크
    @Override
    public Boolean checkMessagePermission(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + memberId));

        String accessToken = member.getKakaoAccessToken(); // 이미 저장된 Kakao Access Token

        Map<String, Object> scopesResponse = getKakaoScopes(accessToken);
        List<Map<String, Object>> scopes = (List<Map<String, Object>>) scopesResponse.get("scopes");

        // talk_message 권한 여부 확인
        boolean isMessageToKakao = scopes.stream()
            .anyMatch(
                scope -> "talk_message".equals(scope.get("id")) && (Boolean) scope.get("agreed"));

        // 카카오톡 로그인인지 아닌지
        if (member.getSocialLogin() != null) {

            // 최초 가입 할 시 권한 허용이 true 면
            if (isMessageToKakao) {

                //  가입한 회원이 이미 권한 허용 했으면
                if (member.isMessageToKakao()) {

                    return member.isMessageToKakao();
                }

                member.updateKakaoMessageIsAgree(isMessageToKakao);
                member.updateIsMessageToKakao(isMessageToKakao);
            }
            return isMessageToKakao;
        } else {
            return null;
        }
    }

    // 카카오 회원의 카카오톡 메시지 권한을 수정
    @Override
    public Boolean updateMessagePermission(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + memberId));

        Boolean KakaoMessageIsAgree = member.isKakaoMessageIsAgree();

        if (KakaoMessageIsAgree != null) {

            member.updateKakaoMessageIsAgree(!KakaoMessageIsAgree);

        } else {

            return null;

        }

        return member.isKakaoMessageIsAgree();
    }

    // 카카오톡 메시지를 받을지 회원이 체크하는 메서드
    @Override
    public boolean updateMessageToKakao(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + memberId));

        Boolean KakaoMessageIsAgree = member.isMessageToKakao();

        if (KakaoMessageIsAgree != null) {

            member.updateIsMessageToKakao(!KakaoMessageIsAgree);

        }

        return member.isMessageToKakao();
    }

    // 책장 공개 여부 설정
    @Override
    public boolean updateIsPublic(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + memberId));

        member.updateIsPublic(!member.isPublic());

        return member.isPublic();
    }

    @Override
    public void deleteMember(Long memberId) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + memberId));

        member.updateDeleted(true);
        member.updateDeletedAt(LocalDateTime.now());

    }

    // 내 정보 불러오기
    @Override
    public MyPageResponseDTO getMyInformation(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + memberId));

        // DTO 변환
        return MyPageResponseDTO.builder()
            .id(member.getId())
            .nickname(member.getNickname())
            .profileImage(member.getProfileImage())
            .birth(member.getBirth())
            .gender(member.getGender())
            .isSocial(member.getSocialLogin() != null)
            .preferences(member.getPreferences())
            .isPublic(member.isPublic())
            .isMessageToKakao(member.isMessageToKakao())
            .KakaoMessageIsAgree(member.isKakaoMessageIsAgree())
            .honorific(member.getHonorific())
            .createdDate(member.getCreatedDate())
            .build();
    }

    // 카카오 API 호출 로직을 별도로 분리
    private Map<String, Object> getKakaoScopes(String accessToken) {
        String kakaoCheckScopeUrl = "https://kapi.kakao.com/v2/user/scopes";

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
            kakaoCheckScopeUrl, HttpMethod.GET, entity, Map.class
        );

        return response.getBody();
    }

    // 프로필 변경(프로필 사진, 닉네임)
    // 닉네임 유효성 체킹은 프론트에서 함
    @Override
    public Map<String, String> updateNicknameAndProfileImage(Long memberId, String nickname,
        MultipartFile profileImage)
        throws IOException, NoSuchAlgorithmException {

        boolean isUpdated = false;
        Map<String, String> result = new HashMap<>();

        // 유저 찾기
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        // 닉네임 변경
        if (nickname != null && !member.getNickname().equals(nickname)) {
            member.setNickname(nickname);
            result.put("nickname", "success");

            isUpdated = true;
        }

        // 프로필 변경
        if (profileImage != null) {
            String currentProfileImage = member.getProfileImage();

            if (!"defaultProfileImage.jpg".equals(currentProfileImage)) {
                String prevKey = s3Service.extractKeyFromUrl(currentProfileImage);
                s3Service.deleteFile(prevKey);
                result.put("deletePrevProfileImage", "success");
            }

            String key = s3Service.generateFileKey(profileImage.getOriginalFilename());
            String newProfileImage = s3Service.uploadFile(key, profileImage);
            member.updateProfileImage(newProfileImage);
            result.put("profileImage", "success");

            isUpdated = true;
        }

        if (isUpdated) {
            memberRepository.save(member);
        }

        return result;
    }

    // 프로필 이미지 없애기
    @Override
    public Map<String, String> dropProfileImage(Long memberId) {
        Map<String, String> result = new HashMap<>();

        // 유저 찾기
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        if (member.getProfileImage().equals("defaultProfileImage.jpg")) {
            result.put("dropProfileImage", "success");
            return result;
        }

        String key = s3Service.extractKeyFromUrl(member.getProfileImage());
        s3Service.deleteFile(key);

        member.updateProfileImage("defaultProfileImage.jpg");

        memberRepository.save(member);

        result.put("dropProfileImage", "success");

        return result;
    }

    // 생년월일, 성별 수정
    @Override
    public Map<String, String> updateBirthAndGender(Long memberId, LocalDate birth, String gender) {

        boolean isUpdated = false;
        Map<String, String> result = new HashMap<>();

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        if (birth != null && !birth.equals(member.getBirth())) {
            member.updateBirth(birth);
            isUpdated = true;
            result.put("birth", "success");
        }

        if (gender != null && !gender.equals(member.getGender())) {
            member.updateGender(gender);
            isUpdated = true;
            result.put("gender", "success");
        }

        if (isUpdated) {
            memberRepository.save(member);
        }

        return result;
    }

    // 취향 수정
    @Override
    public Map<String, String> updatePreferences(Long memberId, List<String> preferences) {
        if (preferences == null || preferences.isEmpty()) {
            throw new IllegalStateException("취향이 선택되어지지 않았습니다.");
        }

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        member.clearPreferences();

        for (String preference : preferences) {
            try {
                member.getPreferences().add(PreferencesType.valueOf(preference));
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("잘못된 취향 값: " + preference, e);
            }
        }

        memberRepository.save(member);

        return Map.of("preferences", "success");
    }

    // 기존 비번과 동일한지 체킹
    @Override
    public boolean checkSamePassword(Long memberId, String password) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        log.info("---------------------" + password);

        String prevPassword = member.getPassword();

        return passwordEncoder.matches(password, prevPassword);
    }

    // 수정하기 버튼 누를때
    // 한번더 동일한지 체킹
    @Override
    public Map<String, String> updatePassword(Long memberId, String password) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        Map<String, String> result = new HashMap<String, String>();

        if (checkSamePassword(memberId, password)) {
            result.put("password", "fail");
            result.put("message", "기존 비밀번호와 동일합니다.");
            return result;
        } else {
            member.updatePassword(passwordEncoder.encode(password));
            result.put("password", "success");
        }

        memberRepository.save(member);

        return result;
    }
}
