package com.undefinedus.backend.dto.request.report;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportRequestDTO {

    private Long reportedId;  // 신고 당한 사람 // 댓글, 게시물 만든 유저
    private String targetType;  // 신고 대상 타입 (Discussion, Comment 등)
    private String reason;  // 신고 대상 타입 (Discussion, Comment 등)
    private Long discussionId;  // 신고 당한 토론 게시물 // comment가 존재하면 null
    private Long commentId;  // 신고 당한 토론 댓글 게시물 // discussion가 존재하면 null

}