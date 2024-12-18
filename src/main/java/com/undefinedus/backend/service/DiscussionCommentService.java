package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentsScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentRequestDTO;
import com.undefinedus.backend.dto.response.discussionComment.DiscussionCommentResponseDTO;
import java.util.List;
import java.util.Map;

public interface DiscussionCommentService {

    Map<String, Object> writeComment(Long discussionId, Long memberId,
        DiscussionCommentRequestDTO discussionCommentRequestDTO);

    Map<String, Object> writeReply(Long discussionId, Long discussionCommentId, Long memberId,
        DiscussionCommentRequestDTO discussionCommentRequestDTO);

    ScrollResponseDTO<DiscussionCommentResponseDTO> getCommentList(
        Long loginMemberId, DiscussionCommentsScrollRequestDTO discussionCommentsScrollRequestDTO, Long discussionId);

    DiscussionCommentResponseDTO addLike(Long memberId, Long discussionCommentId);

    DiscussionCommentResponseDTO addDislike(Long memberId, Long discussionCommentId);

    void deleteComment(Long memberId, Long commentId);

    List<DiscussionCommentResponseDTO> getBest3CommentByCommentLikes(Long loginMemberId, Long discussionId);

    DiscussionCommentResponseDTO getCommentDTO(DiscussionComment discussionComment, Member member);
}
