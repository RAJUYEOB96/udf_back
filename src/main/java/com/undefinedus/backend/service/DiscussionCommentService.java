package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.request.DiscussionCommentsScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentRequestDTO;
import com.undefinedus.backend.dto.response.discussionComment.DiscussionCommentListResponseDTO;

public interface DiscussionCommentService {

    void writeComment(Long discussionId, Long memberId,
        DiscussionCommentRequestDTO discussionCommentRequestDTO);

    void writeReply(Long discussionId, Long discussionCommentId, Long memberId,
        DiscussionCommentRequestDTO discussionCommentRequestDTO);

    ScrollResponseDTO<DiscussionCommentListResponseDTO> getCommentList(
        DiscussionCommentsScrollRequestDTO discussionCommentsScrollRequestDTO);

    void addLike(Long memberId, Long discussionCommentId);

    void addDislike(Long memberId, Long discussionCommentId);

    void deleteComment(Long memberId, Long commentId);
}
