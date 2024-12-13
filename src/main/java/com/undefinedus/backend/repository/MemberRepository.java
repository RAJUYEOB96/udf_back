package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.repository.queryDSL.MemberRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// softDelete이기 때문에 Jpql이나 QueryDSL 사용시 where 절에 softDelete 관련 적기
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    @Query("select m from Member m "
        + "left join fetch m.memberRoleList  mr "
        + "left join fetch m.preferences p "
        + "left join fetch m.socialLogin s "  // socialLogins fetch join 추가
        + "where m.username = :username "
        + "and m.isDeleted = false")
    Optional<Member> getWithRoles(@Param("username") String username); // CustomUserDetailsService에서 사용중
    
    // username으로 조회할 때도 탈퇴하지 않은 회원만 조회
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.isDeleted = false")
    Optional<Member> findByUsername(String username);
    
    // nickname으로 조회할 때도 탈퇴하지 않은 회원만 조회
    @Query("SELECT m FROM Member m WHERE m.nickname = :nickname AND m.isDeleted = false")
    Optional<Member> findByNickname(String nickname);
    
    @Query("SELECT m FROM Member m WHERE m.id = :id AND m.isDeleted = false")
    Optional<Member> findByIdAndIsDeletedFalse(@Param("id") Long id);
    
}
