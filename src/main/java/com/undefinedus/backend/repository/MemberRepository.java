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

    Optional<Member> findByUsername(String username);
    
    Optional<Member> findByNickname(String nickname);
    
    // 아래는 initData할때 필요한 sql, 추후 삭제 될 수 있음
    @Query("SELECT m FROM Member m " +
            "LEFT JOIN FETCH m.followings " +
            "LEFT JOIN FETCH m.followers " +
            "WHERE m.username = :username")
    Optional<Member> findByUsernameWithFollows(@Param("username") String username);
    
    // 아래는 initData할때 필요한 sql, 추후 삭제 될 수 있음
    @Modifying
    @Query(value = "DELETE FROM follow", nativeQuery = true)
    void deleteAllFollows();

//    // 모든 회원 중 isMessageToKakao = true인 회원들의 id를 가져옴
//    @Query("select m.id from Member m where m.isMessageToKakao = true")
//    List<Long> findMessageToKakaoMemberIdList();
}
