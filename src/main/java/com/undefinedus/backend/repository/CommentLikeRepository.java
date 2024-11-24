package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.CommentLike;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentAndMember(DiscussionComment comment, Member member);
}
