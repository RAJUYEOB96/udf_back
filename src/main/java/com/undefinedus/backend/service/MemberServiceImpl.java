package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.SocialLogin;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.social.RegisterRequestDTO;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.util.JWTUtil;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final MyPageService myPageService;
    private final KakaoTalkService kakaoTalkService;

    private final PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> getKakaoInfo(String kakaoAccessToken, String kakaoRefreshToken) {

        Map<String, Object> result = new HashMap<>();

        // accessToken을 이용해서 사용자의 정보를 가져오기
        Map<String, String> kakaoInfo = getKakaoIdAndNicknameFromKakaoAccessToken(
            kakaoAccessToken);

        // 기존에 DB에 회원 정보가 있는 경우 / 없는 경우
        Optional<Member> isRegister = memberRepository.findByUsername(
            "kakao_" + kakaoInfo.get("kakaoId"));

        if (isRegister.isPresent()) {

            isRegister.get().updateKakaoAccessToken(kakaoAccessToken);
            isRegister.get().updateKakaoRefreshToken(kakaoRefreshToken);

            MemberSecurityDTO memberSecurityDTO = entityToDTOWithSocial(isRegister.get());

            Map<String, Object> claims = memberSecurityDTO.getClaims();

            String accessToken = JWTUtil.generateAccessToken(claims);
            String refreshToken = JWTUtil.generateRefreshToken(claims);

            claims.put("accessToken", accessToken);
            claims.put("refreshToken", refreshToken);
            claims.put("kakaoRefreshToken", kakaoRefreshToken);
            claims.put("kakaoAccessToken", kakaoAccessToken);

            result.put("result", "exists");
            result.put("member", claims);

            return result;
        }

        result.put("kakaoId", kakaoInfo.get("kakaoId"));
        result.put("kakaoAccessToken", kakaoAccessToken);
        result.put("kakaoRefreshToken", kakaoRefreshToken);
        result.put("result", "new");



        return result;
    }

    @Override
    public MemberSecurityDTO socialRegister(RegisterRequestDTO requestDTO) {

        Member socialMember = makeSocialMember(requestDTO);

        // 회원 가입 시 토큰 저장
        socialMember.updateKakaoAccessToken(requestDTO.getKakaoAccessToken());
        socialMember.updateKakaoRefreshToken(requestDTO.getKakaoRefreshToken());


        Member savedMember = memberRepository.save(socialMember);
        myPageService.checkMessagePermission(savedMember.getId());

        return entityToDTOWithSocial(savedMember);
    }

    @Override
    public MemberSecurityDTO regularRegister(RegisterRequestDTO requestDTO) {
        Member member = Member.builder()
            .username(requestDTO.getUsername())
            .password(passwordEncoder.encode(requestDTO.getPassword()))
            .nickname(requestDTO.getNickname())
            // 나중에 수정되면 바꿔야함
            .profileImage("defaultProfileImage.jpg")
            .birth(requestDTO.getBirth())
            .gender(requestDTO.getGender())
            .build();

        member.getMemberRoleList().add(MemberType.USER);

        List<String> preferences = requestDTO.getPreferences();
        if (preferences != null || !preferences.isEmpty()) {
            for (String preference : preferences) {
                member.getPreferences().add(PreferencesType.valueOf(preference));
            }
        } else {
            throw new IllegalStateException("취향이 선택되어지지 않았습니다.");
        }

        Member savedMember = memberRepository.save(member);

        return entityToDTOWithRegular(savedMember);
    }

    @Override
    public void usernameDuplicateCheck(String username) {
        Optional<Member> findMember = memberRepository.findByUsername(username);

        if (findMember.isPresent()) {
            log.error("Username 이 '{}' 가 이미 존재합니다.", username);
            throw new IllegalArgumentException("해당 username이 이미 존재합니다.");
        }

    }

    @Override
    public void nicknameDuplicateCheck(String nickname) {
        Optional<Member> findMember = memberRepository.findByNickname(nickname);

        if (findMember.isPresent()) {
            log.error("Nickname 이 '{}' 가 이미 존재합니다.", nickname);
            throw new IllegalArgumentException("해당 nickname이 이미 존재합니다.");
        }

    }

    private Member makeSocialMember(RegisterRequestDTO requestDTO) {

        // 소셜 로그인 비밀번호는 사용자가 사용하진 않지만 최소한의 보안은 하도록 아래처럼
        String tempPassword = "pw_" + requestDTO.getUsername();

        log.info("tempPassword : " + tempPassword);

        Member member = Member.builder()
            .username(requestDTO.getUsername())
            .password(passwordEncoder.encode(tempPassword))
            .nickname(requestDTO.getNickname())
            // 나중에 수정되면 바꿔야함
            .profileImage("defaultProfileImage.jpg")
            .birth(requestDTO.getBirth())
            .gender(requestDTO.getGender())
            .build();

        String[] split = requestDTO.getUsername().split("_");
        String providerId = split[1];

        SocialLogin socialLogin = SocialLogin.builder()
            .member(member)
            .provider("KAKAO")
            .providerId(providerId)
            .build();

        member.setSocialLogin(socialLogin);
        member.getMemberRoleList().add(MemberType.USER);

        List<String> preferences = requestDTO.getPreferences();
        if (preferences != null || !preferences.isEmpty()) {
            for (String preference : preferences) {
                member.getPreferences().add(PreferencesType.valueOf(preference));
            }
        } else {
            throw new IllegalStateException("취향이 선택되어지지 않았습니다.");
        }

        return member;
    }

    private Map<String, String> getKakaoIdAndNicknameFromKakaoAccessToken(String accessToken) {

        String kakaoGetUserUrl = "https://kapi.kakao.com/v2/user/me";

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        UriComponents uriBuilder = UriComponentsBuilder.fromHttpUrl(kakaoGetUserUrl).build();

        ResponseEntity<LinkedHashMap> response =
            restTemplate.exchange(uriBuilder.toUri(), HttpMethod.GET, entity, LinkedHashMap.class);

        log.info("response : " + response);

        LinkedHashMap<String, LinkedHashMap> bodyMap = response.getBody();

        log.info("--------------------------------");
        log.info("bodyMap : " + bodyMap);

        String kakaoId = String.valueOf(bodyMap.get("id"));
        LinkedHashMap<String, String> kakaoAccount = bodyMap.get("properties");

        log.info("kakaoAccount : " + kakaoAccount);

        String nickname = kakaoAccount.get("nickname");

        log.info("kakaoId : " + kakaoId);

        return Map.of("kakaoId", kakaoId);
    }
}
