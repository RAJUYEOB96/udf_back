package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentsScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentRequestDTO;
import com.undefinedus.backend.dto.response.discussionComment.DiscussionCommentResponseDTO;
import java.util.List;

public interface DiscussionCommentService {

    void writeComment(Long discussionId, Long memberId,
        DiscussionCommentRequestDTO discussionCommentRequestDTO);

    void writeReply(Long discussionId, Long discussionCommentId, Long memberId,
        DiscussionCommentRequestDTO discussionCommentRequestDTO);

    ScrollResponseDTO<DiscussionCommentResponseDTO> getCommentList(
        Long loginMemberId, DiscussionCommentsScrollRequestDTO discussionCommentsScrollRequestDTO, Long discussionId);

    void addLike(Long memberId, Long discussionCommentId);

    void addDislike(Long memberId, Long discussionCommentId);

    void deleteComment(Long memberId, Long commentId);

    List<DiscussionCommentResponseDTO> getBest3CommentByCommentLikes(Long discussionId);
}
