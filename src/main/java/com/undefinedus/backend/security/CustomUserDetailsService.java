package com.undefinedus.backend.security;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.SocialLogin;
import com.undefinedus.backend.dto.MemberDTO;
import com.undefinedus.backend.dto.SocialLoginDTO;
import com.undefinedus.backend.repository.MemberRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;


    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<Member> member = memberRepository.getWithRoles(username);

        if (member.isEmpty()) {
            throw new UsernameNotFoundException("해당 Member를 찾을 수 없습니다. : " + username);
        }

        // MemberDTO 생성 시 SocialLogin을 SocialLoginDTO로 변환
//        SocialLogin socialLogin = member.get().getSocialLogin();
//        SocialLoginDTO socialLoginDTO;
//        if (socialLogin != null) {
//            socialLoginDTO = new SocialLoginDTO(
//                socialLogin.getId(),
//                socialLogin.getMember().getId(),
//                socialLogin.getProvider(),
//                socialLogin.getProviderId()
//            );
//        } else {
//            socialLoginDTO = new SocialLoginDTO();
//        }
        
        // 일반로그인은 social이 없기때문에 빈 DTO를 생성후 넣어주기
        SocialLoginDTO socialLoginDTO = new SocialLoginDTO();
        
        MemberDTO memberDTO = new MemberDTO(
            member.get().getUsername(),
            member.get().getPassword(),
            member.get().getNickname(),
            socialLoginDTO,
            member.get().getMemberRoleList()
                .stream()
                .map(memberType -> memberType.name()).collect(Collectors.toList())
        );

        log.info(memberDTO);

        return memberDTO;
    }
}
