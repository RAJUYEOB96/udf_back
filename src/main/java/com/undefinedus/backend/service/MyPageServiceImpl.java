package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.MemberRepository;
import jakarta.transaction.Transactional;
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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Service
@Log4j2
@Transactional
public class MyPageServiceImpl implements MyPageService {

    private final MemberRepository memberRepository;

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
}
