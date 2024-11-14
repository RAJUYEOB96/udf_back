package com.undefinedus.backend.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.SocialLogin;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.repository.MemberRepository;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Log4j2
class SecurityTest {
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Test
    @DisplayName("일반 회원 MemberSecurityDTO 변환 테스트")
    void testRegularMemberConversion() {
        // given
        Member member = Member.builder()
                .username("test@test.com")
                .password("password123")
                .nickname("테스터")
                .build();
        member.addRole(MemberType.USER);
        memberRepository.save(member);
        
        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(member.getUsername());
        
        // then
        assertAll(
                () -> assertThat(userDetails).isInstanceOf(MemberSecurityDTO.class),
                () -> {
                    MemberSecurityDTO dto = (MemberSecurityDTO) userDetails;
                    assertThat(dto.getUsername()).isEqualTo("test@test.com");
                    assertThat(dto.getNickname()).isEqualTo("테스터");
                    assertThat(dto.getRoles()).contains("USER");
                    assertThat(dto.getSocialProvider()).isEqualTo("일반");
                    assertThat(dto.getAuthorities())
                            .contains(new SimpleGrantedAuthority("ROLE_USER"));
                }
        );
    }
    
    @Test
    @DisplayName("소셜 회원 MemberSecurityDTO 변환 테스트")
    void testSocialMemberConversion() {
        // given
        Member member = Member.builder()
                .username("12345")
                .password("password123")
                .nickname("카카오테스터")
                .build();
        member.addRole(MemberType.USER);
        
        SocialLogin socialLogin = SocialLogin.builder()
                .provider("kakao")
                .providerId("12345")
                .member(member)
                .build();
        member.setSocialLogin(socialLogin);
        
        memberRepository.save(member);
        
        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(member.getUsername());
        
        // then
        assertAll(
                () -> assertThat(userDetails).isInstanceOf(MemberSecurityDTO.class),
                () -> {
                    MemberSecurityDTO dto = (MemberSecurityDTO) userDetails;
                    assertThat(dto.getUsername()).isEqualTo("12345");
                    assertThat(dto.getNickname()).isEqualTo("카카오테스터");
                    assertThat(dto.getRoles()).contains("USER");
                    assertThat(dto.getSocialProvider()).isEqualTo("kakao");
                    assertThat(dto.getAuthorities())
                            .contains(new SimpleGrantedAuthority("ROLE_USER"));
                }
        );
    }
    
    @Test
    @DisplayName("존재하지 않는 사용자 조회시 예외 발생")
    void testUserNotFound() {
        // when & then
        assertThrows(UsernameNotFoundException.class, () ->
                userDetailsService.loadUserByUsername("nonexistent@test.com")
        );
    }
    
    @Test
    @DisplayName("MemberSecurityDTO Claims 생성 테스트")
    void testMemberSecurityDTOClaims() {
        // given
        MemberSecurityDTO dto = new MemberSecurityDTO(
                "test@test.com",
                "password123",
                10005L,
                "테스터",
                List.of("USER"),
                "일반"
        );
        
        // when
        Map<String, Object> claims = dto.getClaims();
        
        // then
        assertAll(
                () -> assertThat(claims).containsKey("username"),
                () -> assertThat(claims).containsKey("nickname"),
                () -> assertThat(claims).containsKey("roles"),
                () -> assertThat(claims).containsKey("socialProvider"),
                () -> assertThat(claims.get("username")).isEqualTo("test@test.com"),
                () -> assertThat(claims.get("nickname")).isEqualTo("테스터"),
                () -> assertThat((List<String>)claims.get("roles")).contains("USER"),
                () -> assertThat(claims.get("socialProvider")).isEqualTo("일반")
        );
    }
    
    @Test
    @DisplayName("회원 권한 확인 테스트")
    void testMemberAuthorities() {
        // given
        Member member = Member.builder()
                .username("admin2@test2.com")
                .password("password123")
                .nickname("관리자2")
                .build();
        member.addRole(MemberType.USER);
        member.addRole(MemberType.ADMIN);
        memberRepository.save(member);
        
        // when
        UserDetails userDetails = userDetailsService.loadUserByUsername(member.getUsername());
        
        // then
        assertAll(
                () -> assertThat(userDetails.getAuthorities())
                        .extracting(auth -> auth.getAuthority())
                        .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN"),
                () -> {
                    MemberSecurityDTO dto = (MemberSecurityDTO) userDetails;
                    assertThat(dto.getRoles()).contains("USER", "ADMIN");
                }
        );
    }
}
