package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.Report;
import com.undefinedus.backend.domain.enums.DiscussionCommentStatus;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.domain.enums.ReportStatus;
import com.undefinedus.backend.domain.enums.ReportTargetType;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.report.ReportRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.report.ReportResponseDTO;
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
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
@Transactional
public class ReportServiceImpl implements ReportService {
    
    private static final String REPORT_NOT_FOUND = "해당 report를 찾을 수 없습니다. : %d";

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
            .previousDiscussionStatus(discussion != null ? discussion.getStatus() : null)
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
    
    @Override
    public ScrollResponseDTO<ReportResponseDTO> getReportList(ScrollRequestDTO requestDTO) {
        
        // 해당 tabCondition에 따른 전체 갯수
        Long totalElements = reportRepository.countReportListByTabCondition(requestDTO);
        
        // size + 1개 데이터 조회해서 가져옴 (size가 10이면 11개 가져옴)
        List<Report> findReports = reportRepository.getReportListByTabCondition(requestDTO);
        
        boolean hasNext = false;
        if (findReports.size() > requestDTO.getSize()) { // 11 > 10 이면 있다는 뜻
            hasNext = true;
            findReports.remove(findReports.size() - 1); // 11개 가져온 걸 10개를 보내기 위해
        }
        
        List<ReportResponseDTO> dtoList =
                findReports.stream().map(report -> ReportResponseDTO.from(report)).collect(Collectors.toList());
        
        // 마지막 항목의 ID 설정
        Long lastId = findReports.isEmpty() ?
                requestDTO.getLastId() :    // 조회된 목록이 비어있는 경우를 대비해 삼항 연산자 사용
                findReports.get(findReports.size() - 1).getId(); // lastId를 요청 DTO의 값이 아닌, 실제 조회된 마지막 항목의 ID로 설정
        
        return ScrollResponseDTO.<ReportResponseDTO>withAll()
                .content(dtoList)
                .hasNext(hasNext)
                .lastId(lastId)
                .numberOfElements(dtoList.size())
                .totalElements(totalElements)
                .build();
    }
    
    @Override
    public ReportResponseDTO getReportDetail(Long reportId) {
        
        Report report = reportRepository.findByIdWithAll(reportId)
                .orElseThrow(() -> new ReportNotFoundException(String.format(REPORT_NOT_FOUND, reportId)));
        
        return ReportResponseDTO.from(report);
    }
    
    @Override
    public void rejectReport(Long reportId) {
        
        Report report = reportRepository.findByIdWithAll(reportId)
                .orElseThrow(() -> new ReportNotFoundException(String.format(REPORT_NOT_FOUND, reportId)));
        
        // 이미 처리된 신고인지 확인
        if (report.getStatus() != ReportStatus.TEMPORARY_ACCEPTED && report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("확정되지 않은 신고만 거절할 수 있습니다.");
        }
        
        report.changeStatus(ReportStatus.REJECTED);
        
        Discussion discussion = report.getDiscussion();
        DiscussionComment discussionComment = report.getComment();
        
        if (discussion != null && report.getPreviousDiscussionStatus() != null) {
            discussion.changeStatus(report.getPreviousDiscussionStatus());
        }
        
        if (discussionComment != null) {
            discussionComment.changeDiscussionCommentStatus(DiscussionCommentStatus.ACTIVE);
        }
        
    }
    
}
