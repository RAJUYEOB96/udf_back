package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.DiscussionComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionCommentRepository extends JpaRepository<DiscussionComment, Long> {

}
