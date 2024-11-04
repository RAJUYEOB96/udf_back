package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.MemberType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
// 김용
public class MemberRepositoryTests {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
}