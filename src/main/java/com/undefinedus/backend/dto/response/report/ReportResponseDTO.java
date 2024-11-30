package com.undefinedus.backend.dto.response.report;

import com.undefinedus.backend.domain.entity.Report;
import com.undefinedus.backend.domain.enums.ReportTargetType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReportResponseDTO {
    
    private Long id;             // 신고 ID

    private String reporterNickname; // 신고자
    private String reportedNickname;   // 신고당한 사람
    
    private String reportReason;    // 신고 사유, 종류
    
    private LocalDateTime reportTime;   // 신고한 시간 (createdDate)
    private ReportTargetType targetType;  // 신고 대상 타입 (Discussion, Comment 등)
    
    private String discussionTitle;     // 신고한 토론 게시물 제목
    private String discussionContent;    // 신고한 토론 게시물 내용
    
    private String commentContent;    // 신고한 댓글 내용
    
    
    public static ReportResponseDTO from(Report report) {
        return ReportResponseDTO.builder()
                .id(report.getId())
                .reporterNickname(report.getReporter().getNickname())
                .reportedNickname(report.getReported().getNickname())
                .reportReason(report.getReportReason())
                .reportTime(report.getCreatedDate())
                .targetType(report.getTargetType())
                .discussionTitle(report.getDiscussion() != null ? report.getDiscussion().getTitle() : null)
                .discussionContent(report.getDiscussion()!= null? report.getDiscussion().getContent() : null)
                .commentContent(report.getComment()!= null? report.getComment().getContent() : null)
                .build();
    }
}