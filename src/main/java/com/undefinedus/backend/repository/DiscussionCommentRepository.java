package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.enums.VoteType;
import com.undefinedus.backend.repository.queryDSL.DiscussionCommentsRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface DiscussionCommentRepository extends JpaRepository<DiscussionComment, Long>,
    DiscussionCommentsRepositoryCustom {

    @Query(nativeQuery = true,
        value = "SELECT dc.comment_order FROM discussion_comment dc " +
            "WHERE dc.discussion_id = :discussionId " +
            "AND dc.parent_id = :discussionCommentId " +
            "ORDER BY dc.comment_order DESC " +
            "LIMIT 1")
    Optional<Long> findTopOrder(@Param("discussionId") Long discussionId,
        @Param("discussionCommentId") Long discussionCommentId);

    @Query("SELECT COALESCE(MAX(dc.groupId), 0) FROM DiscussionComment dc")
    Long findMaxGroupId();

    @Query(nativeQuery = true,
        value = "SELECT dc.total_order FROM discussion_comment dc " +
            "WHERE dc.discussion_id = :discussionId " +
            "ORDER BY dc.total_order DESC " +
            "LIMIT 1")
    Optional<Long> findTopTotalOrder(@Param("discussionId") Long discussionId);

    // 없으면 0을 반환
    @Query("SELECT COALESCE(MAX(dc.totalOrder), 0) "
        + "FROM DiscussionComment dc where dc.groupId = :groupId")
    Long findMaxTotalOrderFromChild(@Param("groupId") Long groupId);

    // startOrder 이상의 totalOrder 값을 가진 모든 DiscussionComment 엔티티의
    // totalOrder를 1씩 증가시킵니다. @Param 어노테이션은 메서드의 startOrder 파라미터를
    @Modifying
    @Transactional
    @Query(nativeQuery = true,
        value = "UPDATE discussion_comment "
            + "SET total_order = total_order + 1 "
            + "WHERE total_order >= :startOrder")
    void incrementTotalOrderFrom(@Param("startOrder") Long startOrder);

    @Query("select count(dc) from DiscussionComment dc"
        + " join dc.discussion d"
        + " where d.id = :discussionId"
        + " and dc.voteType = :voteType")
    long countCommentsForDiscussionAndVoteType(@Param("discussionId") Long discussionId, @Param("voteType") VoteType voteType);

    // SUM(CASE WHEN l.isLike = false THEN 1 ELSE 0 END) ASC: l.isLike가 false인 like 수를 계산하여 싫어요 수가 적은 순서로 정렬
    @Query(nativeQuery = true,
        value = "SELECT dc.* "
            + " FROM discussion_comment dc"
            + " JOIN comment_like l ON dc.id = l.comment_id"
            + " WHERE dc.discussion_id = :discussionId"
            + " AND dc.discussion_comment_status = 'ACTIVE'"
            + " GROUP BY dc.id"
            + " ORDER BY "
            + " COUNT(CASE WHEN l.is_like = true THEN 1 ELSE null END) DESC,"
            + " SUM(CASE WHEN l.is_like = false THEN 1 ELSE 0 END) ASC,"
            + " dc.created_date ASC"
            + " LIMIT 3")
    Optional<List<DiscussionComment>> findBest3CommentList(@Param("discussionId") Long discussionId);
}
