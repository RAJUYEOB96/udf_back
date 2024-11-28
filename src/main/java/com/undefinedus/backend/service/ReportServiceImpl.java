package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.Report;
import com.undefinedus.backend.domain.enums.DiscussionCommentStatus;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.domain.enums.ReportStatus;
import com.undefinedus.backend.domain.enums.ReportTargetType;
import com.undefinedus.backend.dto.request.report.ReportRequestDTO;
import com.undefinedus.backend.dto.response.report.ReportCommentDetailResponseDTO;
import com.undefinedus.backend.dto.response.report.ReportDiscussionDetailResponseDTO;
import com.undefinedus.backend.exception.discussion.DiscussionNotFoundException;
import com.undefinedus.backend.exception.discussionComment.DiscussionCommentNotFoundException;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.exception.report.ReportNotFoundException;
import com.undefinedus.backend.repository.DiscussionCommentRepository;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.ReportRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
@Transactional
public class ReportServiceImpl implements ReportService {

    private final MemberRepository memberRepository;
    private final DiscussionRepository discussionRepository;
    private final ReportRepository reportRepository;
    private final DiscussionCommentRepository discussionCommentRepository;
    private final EntityManager entityManager;


    // 토론, 댓글 신고
    @Override
    public void report(Long reporterId, ReportRequestDTO reportRequestDTO) {

        Long discussionId;
        Long commentId;
        Discussion discussion = null;
        DiscussionComment discussionComment = null;

        if (reportRequestDTO.getDiscussionId() != null) {
            commentId = null;
            discussionId = reportRequestDTO.getDiscussionId();
        } else {
            discussionId = null;
            commentId = reportRequestDTO.getCommentId();
        }

        Long reportedId = reportRequestDTO.getReportedId();
        String reason = reportRequestDTO.getReason();
        String targetType = reportRequestDTO.getTargetType();

        Member member = memberRepository.findById(reporterId)
            .orElseThrow(() -> new MemberNotFoundException("해당 유저를 찾을 수 없습니다 ; " + reporterId));

        Member reported = memberRepository.findById(reportedId)
            .orElseThrow(
                () -> new MemberNotFoundException("해당 신고 당하는 유저를 찾을 수 없습니다 ; " + reportedId));

        if (discussionId != null) {
            discussion = discussionRepository.findById(discussionId)
                .orElseThrow(
                    () -> new DiscussionNotFoundException("해당 토론 방을 찾을 수 없습니다. : " + discussionId));
        } else {
            discussionComment = discussionCommentRepository.findById(commentId)
                .orElseThrow(
                    () -> new DiscussionCommentNotFoundException(
                        "해당 댓글을 찾을 수 없습니다. : " + commentId));
        }

        Report reportDiscussion = Report.builder()
            .reporter(member)
            .reported(reported)
            .comment(discussionComment)
            .discussion(discussion)
            .reportReason(reason)
            .status(ReportStatus.PENDING)
            .targetType(ReportTargetType.valueOf(targetType))
            .build();

        reportRepository.save(reportDiscussion);

        entityManager.flush();

        // 동일한 토론에 대한 신고 수 체크
        if (discussion != null) {
            long discussionReportCount = reportRepository.countByDiscussionAndStatus(discussion,
                ReportStatus.PENDING);
            if (discussionReportCount >= 3) {
                List<Report> discussionReports = reportRepository.findByDiscussionAndStatus(
                    discussion, ReportStatus.PENDING);
                for (Report r : discussionReports) {
                    r.changeStatus(ReportStatus.TEMPORARY_ACCEPTED);
                }
                reportRepository.saveAll(discussionReports);  // 변경된 신고들을 저장

                // 상태 변경 후 토론 상태를 BLOCKED로 변경
                discussion.changeStatus(DiscussionStatus.BLOCKED);
                discussionRepository.save(discussion);  // 변경된 토론 저장
                entityManager.flush();
            }
        }

        // 동일한 댓글에 대한 신고 수 체크
        if (discussionComment != null) {
            long commentReportCount = reportRepository.countByCommentAndStatus(discussionComment,
                ReportStatus.PENDING);
            if (commentReportCount >= 3) {
                List<Report> commentReports = reportRepository.findByCommentAndStatus(
                    discussionComment, ReportStatus.PENDING);
                for (Report r : commentReports) {
                    r.changeStatus(ReportStatus.TEMPORARY_ACCEPTED);
                }
                reportRepository.saveAll(commentReports);  // 변경된 신고들을 저장

                // 상태 변경 후 댓글 상태를 BLOCKED로 변경
                discussionComment.changeDiscussionCommentStatus(DiscussionCommentStatus.BLOCKED);
                discussionCommentRepository.save(discussionComment);  // 변경된 댓글 저장
                entityManager.flush();
            }
        }
    }

    // 토론 신고 보기
    public ReportDiscussionDetailResponseDTO getReportDiscussionDetail(Long reportId) {

        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException("해당 신고 건을 찾을 수 없습니다. : " + reportId));

        ReportDiscussionDetailResponseDTO reportCommentDetailResponseDTO = ReportDiscussionDetailResponseDTO.builder()
            .reportReason(report.getReportReason())
            .reporterMemberName(report.getReporter().getNickname())
            .reportedMemberName(report.getReported().getNickname())
            .reportTime(report.getCreatedDate())
            .targetType(String.valueOf(report.getTargetType()))
            .discussionTitle(report.getDiscussion().getTitle())
            .discussionContent(report.getDiscussion().getContent())
            .build();

        return reportCommentDetailResponseDTO;
    }

    // 댓글 신고 보기
    public ReportCommentDetailResponseDTO getReportCommentDetail(Long reportId) {

        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new ReportNotFoundException("해당 신고 건을 찾을 수 없습니다. : " + reportId));

        ReportCommentDetailResponseDTO reportCommentDetailResponseDTO = ReportCommentDetailResponseDTO.builder()
            .reportReason(report.getReportReason())
            .reporterMemberName(report.getReporter().getNickname())
            .reportedMemberName(report.getReported().getNickname())
            .reportTime(report.getCreatedDate())
            .targetType(String.valueOf(report.getTargetType()))
            .commentContent(report.getComment().getContent())
            .build();

        return reportCommentDetailResponseDTO;
    }
}
