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

    // 카카오톡 메시지를 받을지 회원이 체크하는 메서드
    @Override
    public boolean updateMessageToKakao(Long memberId) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberNotFoundException("해당 회원을 찾을 수 없습니다 : " + memberId));

        Boolean KakaoMessageIsAgree = checkMessagePermission(memberId);

        if (KakaoMessageIsAgree != null) {

            member.updateIsMessageToKakao(!KakaoMessageIsAgree);

        }

        return member.isMessageToKakao();
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
