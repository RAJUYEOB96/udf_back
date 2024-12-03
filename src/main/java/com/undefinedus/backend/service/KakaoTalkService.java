package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBookmark;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookmarkRepository;
import com.undefinedus.backend.scheduler.KakaoTalkSender;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class KakaoTalkService {

    private final KakaoTalkSender kakaoTalkSender;
    private final MemberRepository memberRepository;
    private final MyBookmarkRepository myBookmarkRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendKakaoTalk() {

        List<Long> kakaoMemberIdList = memberRepository.findMessageToKakaoMemberIdList();

        for (Long memberId : kakaoMemberIdList) {

            Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("해당 멤버를 찾을 수 없습니다 : " + memberId));

            String refreshToken = member.getKakaoRefreshToken();

            List<MyBookmark> myBookmarkList = myBookmarkRepository.findByMemberId(memberId);

            int size = myBookmarkList.size();

            int index = (int) (Math.random() * size);

            MyBookmark randomMyBookmark = myBookmarkList.get(index);

            String title = randomMyBookmark.getAladinBook().getTitle();
            String phrase = randomMyBookmark.getPhrase();

            String newAccessToken = updateKakaoAccessToken(member, refreshToken);

            if (newAccessToken != null) {
                log.info("회원 ID {} - 메시지: {}", memberId, phrase);
                kakaoTalkSender.sendMessage(newAccessToken, phrase, title);
            }
        }
    }

    private String updateKakaoAccessToken(Member member, String refreshToken) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        String restApiKey = "433f010a1fa5963afe5402f4fa79bbb4";
        String clientSecretKey = "TZRwcwKgGbDJh9NrqfQPHP9obzZULtNE";

        try {
            // 토큰 갱신을 위한 요청 파라미터
            String postParams = "grant_type=refresh_token"
                + "&client_id=" + restApiKey
                + "&client_secret=" + clientSecretKey
                + "&refresh_token=" + refreshToken;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(postParams, headers);

            // RestTemplate을 사용하여 요청 전송
            ResponseEntity<String> response = restTemplate.exchange(tokenUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                JSONObject jsonResponse = new JSONObject(response.getBody());
                String newAccessToken = jsonResponse.getString("access_token");

                // 새로운 refresh token이 있으면 저장
                if (jsonResponse.has("refresh_token")) {
                    String newRefreshToken = jsonResponse.getString("refresh_token");
                    member.updateKakaoRefreshToken(newRefreshToken);
                    log.info("새로운 리프레시 토큰 저장: {}", newRefreshToken);
                }

                log.info("새로운 액세스 토큰 발급: {}", newAccessToken);
                memberRepository.save(member);
                return newAccessToken;
            } else {
                log.error("액세스 토큰 갱신 실패. 응답 코드: {}, 응답 본문: {}", response.getStatusCode(), response.getBody());
                return null;
            }
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("카카오 인증 오류 (401 Unauthorized): {}", e.getResponseBodyAsString());
            log.error("리프레시 토큰 만료 또는 잘못된 클라이언트 정보일 수 있습니다.");
            return null;
        } catch (HttpClientErrorException e) {
            log.error("HTTP 클라이언트 오류 (상태 코드: {}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("액세스 토큰 갱신 중 예상치 못한 오류 발생: ", e);
            return null;
        }
    }
}
