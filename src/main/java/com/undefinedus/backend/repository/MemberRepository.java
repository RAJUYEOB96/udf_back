package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// softDelete이기 때문에 Jpql이나 QueryDSL 사용시 where 절에 softDelete 관련 적기
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("select m from Member m "
        + "left join fetch m.memberRoleList  mr "
        + "left join fetch m.preferences p "
        + "left join fetch m.socialLogin s "  // socialLogins fetch join 추가
        + "where m.username = :username"
        + " and m.isDeleted = false")
    Optional<Member> getWithRoles(@Param("username") String username); // CustomUserDetailsService에서 사용중
    
    Optional<Member> findByUsername(String username);
    
    Optional<Member> findByNickname(String nickname);
    
    Optional<Member> findByUsernameAndNickname(String kakaoId, String nickname);
}
