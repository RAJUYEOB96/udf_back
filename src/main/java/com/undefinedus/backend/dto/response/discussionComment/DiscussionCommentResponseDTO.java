package com.undefinedus.backend.dto.response.discussionComment;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscussionCommentResponseDTO {

    private Long commentId;

    private Long discussionId;

    private Long memberId;

    private String nickname;

    private String honorific; // 칭호 = 초보리더 // version.1 에서는 기본 칭호를 유지할 예정

    private Long parentId; // 부모Id // 고유 아이디는 id로 사용

    private Long order;

    private Long totalOrder;

    @Builder.Default
    private boolean isChild = false; // id를 조회하고 나온 것을 가지고 isChild count 해서 조회하면 자식의 ord 를 쉽게 알 수 있다.

    private String voteType;  // 찬성/반대 의견

    private String content;  // 댓글 내용

    private Long like;

    private Long dislike;

    @Builder.Default
    private boolean isSelected = false;  // 채택된 답변인지, 댓글의 최상위로 좋아요 많이 받은 3개를 올리기 위한? 잘 모르겠음

    private LocalDateTime createTime;

    private String discussionCommentStatus;
}
