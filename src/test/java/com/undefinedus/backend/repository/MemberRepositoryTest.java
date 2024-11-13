package com.undefinedus.backend.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.undefinedus.backend.domain.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Log4j2
class MemberRepositoryTest {
    
    @Autowired private MemberRepository memberRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private EntityManager em;
    
    @Test
    @DisplayName("memberRepository 연결 테스트")
    void testMemberRepository() {
        assertNotNull(memberRepository);
    }
    
    @Test
    @DisplayName("getWithRoles 메서드 테스트")
    void testGetWithRoles() {
        // given
        String username ="reader1@gmail.com"; // initData에서 생성된 id 2번 테스트용 username
        
        // when
        Member findMember = memberRepository.getWithRoles(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 Member를 찾을 수 없습니다. : " + username));
        
        // then
        assertAll(
                // 1. Member 엔티티 조회 검증
                () -> assertNotNull(findMember),
                () -> assertEquals(username, findMember.getUsername()),
                
                // 2. memberRoleList Fetch Join 검증
                () -> assertDoesNotThrow(() -> {
                    int roleSize = findMember.getMemberRoleList().size();
                    assertTrue(roleSize > 0, "Member는 최소 하나 이상의 Role을 가져야 합니다.");
                }),
                
                // 3. preferences Fetch Join 검증
                () -> assertDoesNotThrow(() -> {
                    int preferenceSize = findMember.getPreferences().size();
                    // 취향은 최소 1개 ~ 최대 3개
                    assertTrue(preferenceSize > 0 && preferenceSize < 4, "Member의 preferences는 null이 아니어야 합니다.");
                
                }),
                
                // 4. Soft Delete 검증
                () -> assertFalse(findMember.isDeleted())
        );
    }
    
    @Test
    @DisplayName("getWithRoles - 삭제된 회원 조회 시 빈 Optional 반환")
    void testGetWithRolesWithDeletedMember() {
        // given
        String username = "deleted@gmail.com";
        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode("1111"))
                .nickname("deletedUser")
                .isDeleted(true)
                .build();
        memberRepository.save(member);
        
        // when
        Optional<Member> result = memberRepository.getWithRoles(username);
        
        // then
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("findByUsername 메서드 테스트")
    void testFindByUsername() {
        // given
        String username = "reader1@gmail.com"; // initData에서 생성된 id 2번 테스트용 username
        
        // when
        Member findMember = memberRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 Member를 찾을 수 없습니다."));
        
        // 영속성 컨텍스트에서 분리
        em.detach(findMember);
        
        // then
        assertAll(
                () -> assertNotNull(findMember),
                () -> assertEquals(username, findMember.getUsername()),
                // LazyInitializationException이 발생하는지 확인
                () -> assertThrows(LazyInitializationException.class,
                        () -> findMember.getMemberRoleList().size())
        );
    }
    
}