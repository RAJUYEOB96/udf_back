package com.undefinedus.backend.repository;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.undefinedus.backend.domain.entity.Follow;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.exception.social.TabConditionNotEqualException;
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
    
    @Test
    @DisplayName("findFollowMembersByTabCondition - 팔로워 목록 조회 테스트")
    void testFindFollowMembersByTabConditionFollowers() {
        // given
        Member targetMember = Member.builder()
                .username("target@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("target")
                .build();
        
        List<Member> followers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            followers.add(Member.builder()
                    .username("follower" + i + "@test.com")
                    .password(passwordEncoder.encode("1111"))
                    .nickname("follower" + i)
                    .build());
        }
        
        memberRepository.save(targetMember);
        memberRepository.saveAll(followers);
        
        // 팔로우 관계 설정
        for (Member follower : followers) {
            Follow follow = Follow.builder()
                    .follower(follower)
                    .following(targetMember)
                    .build();
            em.persist(follow);
        }
        
        em.flush();
        em.clear();
        
        ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                .tabCondition("팔로워")
                .size(10)
                .build();
        
        // when
        List<Member> result = memberRepository.findFollowMembersByTabCondition(targetMember.getId(), requestDTO);
        
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(followers.size(), result.size()),
                () -> assertTrue(result.stream()
                        .allMatch(member -> followers.stream()
                                .anyMatch(f -> f.getNickname().equals(member.getNickname())))
                ));
    }
    
    @Test
    @DisplayName("findFollowMembersByTabCondition - 팔로잉 목록 조회 테스트")
    void testFindFollowMembersByTabConditionFollowings() {
        // given
        Member followerMember = Member.builder()
                .username("follower@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("follower")
                .build();
        
        List<Member> followings = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            followings.add(Member.builder()
                    .username("following" + i + "@test.com")
                    .password(passwordEncoder.encode("1111"))
                    .nickname("following" + i)
                    .build());
        }
        
        memberRepository.save(followerMember);
        memberRepository.saveAll(followings);
        
        // 팔로우 관계 설정
        for (Member following : followings) {
            Follow follow = Follow.builder()
                    .follower(followerMember)
                    .following(following)
                    .build();
            em.persist(follow);
        }
        
        em.flush();
        em.clear();
        
        ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                .tabCondition("팔로잉")
                .size(10)
                .build();
        
        // when
        List<Member> result = memberRepository.findFollowMembersByTabCondition(followerMember.getId(), requestDTO);
        
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(followings.size(), result.size()),
                () -> assertTrue(result.stream()
                        .allMatch(member -> followings.stream()
                                .anyMatch(f -> f.getNickname().equals(member.getNickname())))
                ));
    }
    
    @Test
    @DisplayName("findFollowMembersByTabCondition - 팔로워 닉네임 검색 테스트")
    void testFindFollowMembersByTabConditionWithSearch() {
        // given
        Member targetMember = Member.builder()
                .username("target@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("target")
                .build();
        
        List<Member> searchFollowers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            searchFollowers.add(Member.builder()
                    .username("search" + i + "@test.com")
                    .password(passwordEncoder.encode("1111"))
                    .nickname("search" + i)
                    .build());
        }
        
        Member otherFollower = Member.builder()
                .username("other@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("other")
                .build();
        
        memberRepository.save(targetMember);
        memberRepository.saveAll(searchFollowers);
        memberRepository.save(otherFollower);
        
        // 팔로우 관계 설정
        for (Member follower : searchFollowers) {
            Follow follow = Follow.builder()
                    .follower(follower)
                    .following(targetMember)
                    .build();
            em.persist(follow);
        }
        Follow otherFollow = Follow.builder()
                .follower(otherFollower)
                .following(targetMember)
                .build();
        em.persist(otherFollow);
        
        em.flush();
        em.clear();
        
        ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                .tabCondition("팔로워")
                .search("search")
                .size(10)
                .build();
        
        // when
        List<Member> result = memberRepository.findFollowMembersByTabCondition(targetMember.getId(), requestDTO);
        
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(searchFollowers.size(), result.size()),
                () -> assertTrue(result.stream()
                        .allMatch(member -> member.getNickname().startsWith("search")))
        );
    }
    
    @Test
    @DisplayName("findFollowMembersByTabCondition - 커서 기반 페이징 테스트")
    void testFindFollowMembersByTabConditionWithCursor() {
        // given
        Member targetMember = Member.builder()
                .username("target@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("target")
                .build();
        
        List<Member> followers = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            followers.add(Member.builder()
                    .username("follower" + i + "@test.com")
                    .password(passwordEncoder.encode("1111"))
                    .nickname("follower" + i)
                    .build());
        }
        
        memberRepository.save(targetMember);
        memberRepository.saveAll(followers);
        
        // 팔로우 관계 설정
        for (Member follower : followers) {
            Follow follow = Follow.builder()
                    .follower(follower)
                    .following(targetMember)
                    .build();
            em.persist(follow);
        }
        
        em.flush();
        em.clear();
        
        // 첫 페이지 조회
        ScrollRequestDTO firstRequest = ScrollRequestDTO.builder()
                .tabCondition("팔로워")
                .size(2)
                .build();
        
        List<Member> firstPage = memberRepository.findFollowMembersByTabCondition(
                targetMember.getId(), firstRequest);
        
        // 두 번째 페이지 요청
        Member lastMember = firstPage.get(firstPage.size() - 1);
        ScrollRequestDTO secondRequest = ScrollRequestDTO.builder()
                .tabCondition("팔로워")
                .lastId(lastMember.getId())
                .size(2)
                .build();
        
        // when
        List<Member> secondPage = memberRepository.findFollowMembersByTabCondition(
                targetMember.getId(), secondRequest);
        
        // then
        assertAll(
                () -> assertNotNull(secondPage),
                () -> assertTrue(secondPage.size() <= secondRequest.getSize() + 1),
                () -> assertTrue(secondPage.stream()
                        .allMatch(member ->
                                member.getNickname().compareTo(lastMember.getNickname()) > 0 ||
                                        (member.getNickname().equals(lastMember.getNickname()) &&
                                                member.getId() > lastMember.getId())
                        ))
        );
    }
    
    @Test
    @DisplayName("findFollowMembersByTabCondition - 잘못된 TabCondition 테스트")
    void testFindFollowMembersByTabConditionWithInvalidCondition() {
        // given
        ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                .tabCondition("invalid")
                .build();
        
        // when & then
        assertThrows(TabConditionNotEqualException.class, () ->
                memberRepository.findFollowMembersByTabCondition(1L, requestDTO));
    }
    
    @Test
    @DisplayName("findFollowMembersByTabCondition - 팔로워 닉네임 정렬 순서 테스트")
    void testFindFollowMembersByTabConditionSorting() {
        // given
        Member targetMember = Member.builder()
                .username("target@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("target")
                .build();
        
        // 의도적으로 순서를 섞어서 생성
        Member followerC = Member.builder()
                .username("followerC@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("C_follower")
                .build();
        
        Member followerA = Member.builder()
                .username("followerA@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("A_follower")
                .build();
        
        Member followerB = Member.builder()
                .username("followerB@test.com")
                .password(passwordEncoder.encode("1111"))
                .nickname("B_follower")
                .build();
        
        memberRepository.save(targetMember);
        memberRepository.saveAll(List.of(followerC, followerA, followerB));
        
        // 팔로우 관계 설정
        List<Follow> follows = List.of(
                Follow.builder().follower(followerC).following(targetMember).build(),
                Follow.builder().follower(followerA).following(targetMember).build(),
                Follow.builder().follower(followerB).following(targetMember).build()
        );
        
        for (Follow follow : follows) {
            em.persist(follow);
        }
        
        em.flush();
        em.clear();
        
        ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                .tabCondition("팔로워")
                .size(10)
                .build();
        
        // when
        List<Member> result = memberRepository.findFollowMembersByTabCondition(targetMember.getId(), requestDTO);
        
        // then
        assertAll(
                () -> assertNotNull(result),
                () -> assertEquals(3, result.size()),
                // 닉네임이 사전순으로 정렬되어 있는지 확인
                () -> assertEquals("A_follower", result.get(0).getNickname()),
                () -> assertEquals("B_follower", result.get(1).getNickname()),
                () -> assertEquals("C_follower", result.get(2).getNickname()),
                // 리스트 전체가 정렬되어 있는지 확인
                () -> assertTrue(isNicknameSorted(result))
        );
    }
    
    // 리스트가 닉네임 기준으로 정렬되어 있는지 확인하는 헬퍼 메소드
    private boolean isNicknameSorted(List<Member> members) {
        if (members.size() <= 1) return true;
        
        for (int i = 0; i < members.size() - 1; i++) {
            if (members.get(i).getNickname().compareTo(members.get(i + 1).getNickname()) > 0) {
                return false;
            }
        }
        return true;
    }
}