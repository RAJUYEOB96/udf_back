package com.undefinedus.backend.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.MemberType;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class MemberRepositoryTests {

    private static final Logger log = LoggerFactory.getLogger(MemberRepositoryTests.class);
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("연결 확인 테스트")
    void checkConnections() {
        assertNotNull(memberRepository, "MemberRepository가 주입되지 않았습니다.");
        assertNotNull(passwordEncoder, "PasswordEncoder가 주입되지 않았습니다.");
    }

    @Test
    public void testInsertMember() {

        for (int i = 0; i < 10; i++) {

            Member member = Member.builder()
                .username("user" + i + "@aaa.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("User" + i)
                .build();

            member.addRole(MemberType.USER);

            if (i >= 8) {
                member.addRole(MemberType.ADMIN);
            }

            memberRepository.save(member);
        }
    }

    @Test
    @DisplayName("getWithRoles 메서드 테스트")
    public void testGetWithRoles() {
        Optional<Member> result = memberRepository.getWithRoles("test@aaa.com");
        assertTrue(result.isPresent(), "사용자를 찾지 못했습니다.");
        assertEquals("test@aaa.com", result.get().getUsername());
        assertTrue(result.get().getMemberRoleList().contains(MemberType.ADMIN), "관리자 권한이 존재하지 않습니다.");

        log.info("Member : " + result.get());

    }

    @Test
    @DisplayName("findByUsername 메서드 테스트")
    public void testFindByUsername() {
        Optional<Member> result = memberRepository.findByUsername("test@aaa.com");
        assertTrue(result.isPresent(), "해당 사용자가 존재하지 않습니다.");
        assertEquals("test@aaa.com", result.get().getUsername());
    }
}