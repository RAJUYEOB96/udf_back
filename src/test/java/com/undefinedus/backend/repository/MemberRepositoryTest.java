package com.undefinedus.backend.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
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
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EntityManager em;
    
    @Test
    @DisplayName("memberRepository 연결 테스트")
    void testMemberRepository() {
        assertNotNull(memberRepository);
    }
    
    @Test
    @DisplayName("getWithRoles 메서드 테스트")
    void testGetWithRoles() {
        // given
        String username = "test@test.com";
        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode("1111"))
                .nickname("test")
                .isDeleted(false)
                .build();
        
        // MemberRole과 Preference는 Mock 데이터 추가 필요
        memberRepository.save(member);
        
        // when
        Member findMember = memberRepository.getWithRoles(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 Member를 찾을 수 없습니다. : " + username));
        
        // then
        assertAll(
                // 1. Member 엔티티 조회 검증
                () -> assertNotNull(findMember),
                () -> assertEquals(username, findMember.getUsername()),
                () -> assertFalse(findMember.isDeleted())
        );
    }
    
    @Test
    @DisplayName("getWithRoles - 삭제된 회원 조회 시 빈 Optional 반환")
    void testGetWithRolesWithDeletedMember() {
        // given
        String username = "deleted@test.com";
        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode("1111"))
                .nickname("deleted")
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
        String username = "test@test.com";
        Member member = Member.builder()
                .username(username)
                .password(passwordEncoder.encode("1111"))
                .nickname("test")
                .build();
        memberRepository.save(member);
        
        // when
        Member findMember = memberRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("해당 Member를 찾을 수 없습니다."));
        
        // then
        assertAll(
                () -> assertNotNull(findMember),
                () -> assertEquals(username, findMember.getUsername())
        );
    }
    
    @Test
    @DisplayName("findAllWithoutMemberId - 기본 조회 테스트")
    void testFindAllWithoutMemberId() {
        // given
        
        memberRepository.deleteAll(); // 기존 데이터 삭제
        em.flush();
        em.clear();
        
        Member excludedMember = Member.builder()
                .username("excluded@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("excluded")
                .build();
        
        List<Member> otherMembers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            otherMembers.add(Member.builder()
                    .username("test" + i + "@test.com")
                    .password(passwordEncoder.encode("1111"))
                    .nickname("test" + i)
                    .build());
        }
        
        memberRepository.save(excludedMember);
        memberRepository.saveAll(otherMembers);
        
        ScrollRequestDTO requestDTO = new ScrollRequestDTO();
        requestDTO.setSize(10);
        em.flush();
        em.clear();
        
        // when
        List<Member> members = memberRepository.findAllWithoutMemberId(excludedMember.getId(), requestDTO);
        
        // then
        assertAll(
                () -> assertNotNull(members),
                () -> assertTrue(members.stream()
                        .noneMatch(member -> member.getId().equals(excludedMember.getId()))),
                () -> assertEquals(otherMembers.size(), members.size())
        );
    }
    
    @Test
    @DisplayName("findAllWithoutMemberId - 닉네임 검색 테스트")
    void testFindAllWithoutMemberIdWithNicknameSearch() {
        // given
        Member excludedMember = Member.builder()
                .username("excluded@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("excluded")
                .build();
        
        List<Member> searchMembers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            searchMembers.add(Member.builder()
                    .username("search" + i + "@test.com")
                    .password(passwordEncoder.encode("1111"))
                    .nickname("search" + i)
                    .build());
        }
        
        Member otherMember = Member.builder()
                .username("other@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("other")
                .build();
        
        memberRepository.save(excludedMember);
        memberRepository.saveAll(searchMembers);
        memberRepository.save(otherMember);
        
        ScrollRequestDTO requestDTO = new ScrollRequestDTO();
        requestDTO.setSize(10);
        requestDTO.setSearch("search");
        
        // when
        List<Member> members = memberRepository.findAllWithoutMemberId(excludedMember.getId(), requestDTO);
        
        // then
        assertAll(
                () -> assertNotNull(members),
                () -> assertEquals(searchMembers.size(), members.size()),
                () -> assertTrue(members.stream()
                        .allMatch(member -> member.getNickname().startsWith("search"))),
                () -> assertTrue(members.stream()
                        .noneMatch(member -> member.getId().equals(excludedMember.getId())))
        );
    }
    
    @Test
    @DisplayName("findAllWithoutMemberId - 커서 기반 페이징 테스트")
    void testFindAllWithoutMemberIdWithCursor() {
        // given
        Member excludedMember = Member.builder()
                .username("excluded@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("excluded")
                .build();
        
        List<Member> testMembers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            testMembers.add(Member.builder()
                    .username("test" + i + "@test.com")
                    .password(passwordEncoder.encode("1111"))
                    .nickname("test" + i)
                    .build());
        }
        
        memberRepository.save(excludedMember);
        memberRepository.saveAll(testMembers);
        em.flush();
        em.clear();
        
        // 첫 페이지 조회
        ScrollRequestDTO firstRequest = new ScrollRequestDTO();
        firstRequest.setSize(2);
        List<Member> firstPage = memberRepository.findAllWithoutMemberId(excludedMember.getId(), firstRequest);
        
        // 두 번째 페이지 요청 (lastId와 lastNickname 모두 설정)
        ScrollRequestDTO secondRequest = new ScrollRequestDTO();
        secondRequest.setSize(2);
        
        Member lastMember = firstPage.get(firstPage.size() - 1);
        secondRequest.setLastId(lastMember.getId());
        secondRequest.setLastNickname(lastMember.getNickname());  // lastNickname 추가
        
        // when
        List<Member> secondPage = memberRepository.findAllWithoutMemberId(excludedMember.getId(), secondRequest);
        
        // then
        assertAll(
                () -> assertNotNull(secondPage),
                () -> assertTrue(secondPage.size() <= secondRequest.getSize() + 1),
                () -> assertTrue(secondPage.stream()
                        .allMatch(member ->
                                member.getNickname().compareTo(lastMember.getNickname()) > 0 ||
                                        (member.getNickname().equals(lastMember.getNickname()) &&
                                                member.getId() > lastMember.getId())
                        )),
                () -> assertTrue(secondPage.stream()
                        .noneMatch(member -> member.getId().equals(excludedMember.getId())))
        );
    }
}