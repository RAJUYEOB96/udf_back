package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
    public Boolean checkMessagePermission(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + memberId));

        String accessToken = member.getKakaoAccessToken(); // 이미 저장된 Kakao Access Token

        Map<String, Object> scopesResponse = getKakaoScopes(accessToken);
        List<Map<String, Object>> scopes = (List<Map<String, Object>>) scopesResponse.get("scopes");

        System.out.println("scopes = " + scopes);
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
                member.updateMessageToKakao(isMessageToKakao);
            }
            return isMessageToKakao;
        } else {
            return null;
        }
    }

    // 카카오톡 메시지를 받을지 회원이 체크하는 메서드
    public boolean updateMessageToKakao(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + memberId));

        Boolean KakaoMessageIsAgree = checkMessagePermission(memberId);

        if (KakaoMessageIsAgree != null) {

            member.updateMessageToKakao(!KakaoMessageIsAgree);
            removeMessagePermission(memberId);
        }

        return member.isMessageToKakao();
    }

    // 카카오톡 권한 철회
    private void removeMessagePermission(Long memberId) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + memberId));

        String kakaoAccessToken = member.getKakaoAccessToken();

        // getRevocableScopes 메서드를 사용하여 철회 가능한 권한 목록을 가져옴
        List<String> revocableScopes = getRevocableScopes(memberId);

        // 철회할 권한이 없으면 종료
        if (revocableScopes.isEmpty()) {
            System.out.println("철회할 권한이 없습니다.");
            return;
        }

        String url = "https://kapi.kakao.com/v2/user/revoke/scopes";

        // 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + kakaoAccessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 바디 설정
        Map<String, Object> body = new HashMap<>();
        body.put("scopes", revocableScopes);

        // 요청 엔티티 생성
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // RestTemplate 호출
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            String.class
        );

        // 응답 처리
        System.out.println("Response: " + response.getBody());

    }

    public List<String> getRevocableScopes(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + memberId));

        String accessToken = member.getKakaoAccessToken(); // 저장된 Kakao Access Token

        Map<String, Object> scopesResponse = getKakaoScopes(accessToken);
        List<Map<String, Object>> scopes = (List<Map<String, Object>>) scopesResponse.get("scopes");

        // revocable이 true인 항목의 id를 리스트로 수집
        return scopes.stream()
            .filter(scope -> (Boolean) scope.get("revocable")) // 철회 가능한 항목만 필터링
            .map(scope -> (String) scope.get("id")) // id 값 추출
            .toList(); // 리스트로 변환
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

    @Override
    public boolean checkSamePassword(Long memberId, String password) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        String prevPassword = member.getPassword();

        return passwordEncoder.matches(password, prevPassword);
    }

    @Override
    public Map<String, String> updatePassword(Long memberId, String password) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException("Member Not Found"));

        if (checkSamePassword(memberId, password)) {
            return Map.of("password", "duplicated");
        } else {
            member.updatePassword(passwordEncoder.encode(password));
        }

        return Map.of("password", "success");
    }
}
