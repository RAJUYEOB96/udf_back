package com.undefinedus.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.undefinedus.backend.domain.entity.Follow;
import com.undefinedus.backend.domain.entity.Member;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
class FollowRepositoryTest {
    @Autowired
    private FollowRepository followRepository;
    
    @Autowired
    private EntityManager em;
    
    private Member member1;
    private Member member2;
    private Member member3;
    private Member member4;
    
    @BeforeEach
    void setUp() {
        // 테스트용 Member 생성
        member1 = Member.builder()
                .username("user1@test.com")
                .password("password1")
                .nickname("User1")
                .isPublic(true)
                .build();
        
        member2 = Member.builder()
                .username("user2@test.com")
                .password("password2")
                .nickname("User2")
                .isPublic(true)
                .build();
        
        member3 = Member.builder()
                .username("user3@test.com")
                .password("password3")
                .nickname("User3")
                .isPublic(true)
                .build();
        
        member4 = Member.builder()
                .username("user4@test.com")
                .password("password4")
                .nickname("User4")
                .isPublic(true)
                .build();
        
        // Member 저장
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        
        // Follow 관계 설정
        // member1이 member2, member3를 팔로우
        Follow follow1 = Follow.builder()
                .follower(member1)
                .following(member2)
                .build();
        
        Follow follow2 = Follow.builder()
                .follower(member1)
                .following(member3)
                .build();
        
        // member2, member3, member4가 member1을 팔로우
        Follow follow3 = Follow.builder()
                .follower(member2)
                .following(member1)
                .build();
        
        Follow follow4 = Follow.builder()
                .follower(member3)
                .following(member1)
                .build();
        
        Follow follow5 = Follow.builder()
                .follower(member4)
                .following(member1)
                .build();
        
        em.persist(follow1);
        em.persist(follow2);
        em.persist(follow3);
        em.persist(follow4);
        em.persist(follow5);
        
        em.flush();
        em.clear();
    }
    
    @Test
    @DisplayName("사용자의 팔로잉 수 조회 테스트")
    void countFollowingsByMemberId() {
        // when
        int followingsCount = followRepository.countFollowingsByMemberId(member1.getId());
        
        // then
        assertThat(followingsCount).isEqualTo(2); // member1은 2명을 팔로우
    }
    
    @Test
    @DisplayName("사용자의 팔로워 수 조회 테스트")
    void countFollowersByMemberId() {
        // when
        int followersCount = followRepository.countFollowersByMemberId(member1.getId());
        
        // then
        assertThat(followersCount).isEqualTo(3); // member1은 3명의 팔로워를 가짐
    }
    
    @Test
    @DisplayName("팔로잉이 없는 사용자의 팔로잉 수 조회 테스트")
    void countFollowingsWithNoFollowings() {
        // given
        Member newMember = Member.builder()
                .username("nofollow@test.com")
                .password("password")
                .nickname("NoFollow")
                .isPublic(true)
                .build();
        em.persist(newMember);
        em.flush();
        em.clear();
        
        // when
        int followingsCount = followRepository.countFollowingsByMemberId(newMember.getId());
        
        // then
        assertThat(followingsCount).isZero(); // member4는 아무도 팔로우하지 않음
    }
    
    @Test
    @DisplayName("팔로워가 없는 사용자의 팔로워 수 조회 테스트")
    void countFollowersWithNoFollowers() {
        // given
        Member newMember = Member.builder()
                .username("nofollow@test.com")
                .password("password")
                .nickname("NoFollow")
                .isPublic(true)
                .build();
        em.persist(newMember);
        em.flush();
        em.clear();
        
        // when
        int followersCount = followRepository.countFollowersByMemberId(newMember.getId());
        
        // then
        assertThat(followersCount).isZero(); // member3는 팔로워가 없음
    }
}