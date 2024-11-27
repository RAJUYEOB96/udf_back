package com.undefinedus.backend.dto.request.discussionComment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscussionCommentRequestDTO {

    private String voteType;  // 찬성/반대 의견

    private String content;  // 댓글 내용

}
