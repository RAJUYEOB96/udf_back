package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("select m from Member m "
        + "left join fetch m.memberRoleList  mr "
        + "left join fetch m.preferences p "
        + "where m.username = :username")
    Optional<Member> getWithRoles(@Param("username") String username);
    
    Optional<Member> findByUsername(String username);
    
    Optional<Member> findByNickname(String nickname);
    
    Optional<Member> findByUsernameAndNickname(String kakaoId, String nickname);
}
