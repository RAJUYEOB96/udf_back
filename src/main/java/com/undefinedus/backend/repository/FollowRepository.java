package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Follow;
import com.undefinedus.backend.domain.entity.Member;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    @Query("SELECT COUNT(f) from Follow f "
            + "where f.follower.id = :memberId "
            + "and f.following.isDeleted = false")  // 추가
    
    int countFollowingsByMemberId(@Param("memberId") Long memberId);
    
    @Query("SELECT COUNT(f) from Follow f "
            + "where f.following.id = :memberId "
            + "and f.follower.isDeleted = false")  // 추가
    
    int countFollowersByMemberId(@Param("memberId") Long memberId);
    
    @Query("SELECT f.following.id FROM "
            + "Follow f WHERE f.follower.id = :memberId "
            + "and f.following.isDeleted = false")  // 추가
    
    Set<Long> findFollowingIds(@Param("memberId") Long memberId);
    
    @Query("SELECT f FROM Follow f WHERE f.follower = :follower AND f.following = :following AND f.following.isDeleted = false")
    Optional<Follow> findByFollowerAndFollowing(@Param("follower") Member follower, @Param("following") Member following);
}
