package com.undefinedus.backend.security;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.SocialLogin;
import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.repository.MemberRepository;
import jakarta.transaction.Transactional;
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
        
        Member member = memberRepository.getWithRoles(username)
                .orElseThrow(() -> new UsernameNotFoundException("해당 Member를 찾을 수 없습니다. : " + username));
        
        MemberSecurityDTO memberSecurityDTO = new MemberSecurityDTO(
                member.getUsername(),
                member.getPassword(),
                member.getId(),
                member.getNickname(),
                member.getMemberRoleList()
                        .stream()
                        .map(memberType -> memberType.name())
                        .distinct() // 중복 제거, 왜 중복되어 들어간지 모르겠음, DB안에는 잘 들어가 있는데
                        .collect(Collectors.toList()),
                "일반"
        );
        
        if (member.getSocialLogin() != null) {
            SocialLogin socialLogin = member.getSocialLogin();
            String socialProvider = socialLogin.getProvider();
            memberSecurityDTO.setSocialProvider(socialProvider);
        }
        
        log.info(memberSecurityDTO);
        
        return memberSecurityDTO;
    }
}
