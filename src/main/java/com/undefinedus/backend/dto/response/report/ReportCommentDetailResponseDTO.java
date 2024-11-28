package com.undefinedus.backend.dto.response.report;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportCommentDetailResponseDTO {

    private String reportReason;    // 신고 사유, 종류
    private String reporterMemberName;  // 신고 한 유저 닉네임
    private String reportedMemberName;  // 신고당한 유저 닉네임
    private LocalDateTime reportTime;   // 신고한 시간
    private String targetType;  // 신고 대상 타입 (Discussion, Comment 등)
    private String commentContent;    // 신고한 댓글 내용


}