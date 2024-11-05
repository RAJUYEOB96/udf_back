package com.undefinedus.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.dto.request.social.RegisterRequestDTO;
import com.undefinedus.backend.repository.MemberRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MemberServiceImplTest {

    @Autowired
    private MemberService memberService;
    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("registerMember 메서드 테스트")
    @Commit
    public void testRegisterMember() {

        RegisterRequestDTO memberDTO = RegisterRequestDTO.builder()
                .username("testUser")
                .password("testPassword")
                .nickname("testNickname")
                .birth(LocalDate.now().minusYears(20))
                .gender("남자")
                .preferences(List.of("기타"))
                .build();


        memberService.regularRegister(memberDTO);

        Optional<Member> result = memberRepository.findByUsername("testUser");
        assertTrue(result.isPresent(), "사용자가 등록되지 않았습니다.");

        Member member = result.get();
        assertEquals("testUser", member.getUsername());
        assertTrue(memberRepository.existsById(member.getId()), "사용자가 DB에 존재하지 않습니다.");

        assertNotEquals("testPassword", member.getPassword(), "비밀번호가 인코딩되지 않았습니다.");

        assertEquals("testNickname", member.getNickname());
        assertTrue(member.getMemberRoleList().contains(MemberType.USER), "사용자는 USER 권한을 가져야 합니다.");
        assertTrue(
            member.getPreferences().containsAll(
                Set.of(PreferencesType.소설, PreferencesType.예술)
            ),
            "취향 목록에 BOOKS와 MUSIC이 포함되어야 합니다."
        );
    }
}