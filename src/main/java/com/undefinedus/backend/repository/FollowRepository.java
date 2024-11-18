package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    @Query("SELECT COUNT(f) from Follow f "
            + "where f.follower.id = :memberId")
    int countFollowingsByMemberId(@Param("memberId") Long memberId);
    
    @Query("SELECT COUNT(f) from Follow f "
            + "where f.following.id = :memberId")
    int countFollowersByMemberId(@Param("memberId") Long memberId);
}
