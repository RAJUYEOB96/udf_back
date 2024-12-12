package com.undefinedus.backend.repository.queryDSL;

import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentsScrollRequestDTO;
import java.util.List;

public interface DiscussionCommentsRepositoryCustom {

    List<DiscussionComment> findDiscussionCommentListWithScroll(DiscussionCommentsScrollRequestDTO requestDTO,
            Long discussionId);
}
