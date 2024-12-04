package com.undefinedus.backend.dto.request.discussionComment;

import com.undefinedus.backend.global.validation.annotation.NoProfanity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscussionCommentRequestDTO {

    private String voteType;  // 찬성/반대 의견

    @NoProfanity
    private String content;  // 댓글 내용

}
