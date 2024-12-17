package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.report.ReportRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.discussionComment.DiscussionCommentResponseDTO;
import com.undefinedus.backend.dto.response.report.ReportResponseDTO;
import com.undefinedus.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/discussion/{discussionId}")
    public ResponseEntity<ApiResponseDTO<Void>> reportDiscussion(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable("discussionId") Long discussionId,
        @RequestBody ReportRequestDTO reportRequestDTO
    ) {

        Long memberId = memberSecurityDTO.getId();
        
        reportService.reportDiscussion(memberId, discussionId, reportRequestDTO);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(null));
    }

    @PostMapping("/comment/{commentId}")
    public ResponseEntity<ApiResponseDTO<DiscussionCommentResponseDTO>> reportComment(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @PathVariable("commentId") Long commentId,
        @RequestBody ReportRequestDTO reportRequestDTO
    ) {

        Long memberId = memberSecurityDTO.getId();
        
        // TODO : 신고를 두번 할 수 없음, 그래서 ResponseDTO에 isReport(boolean)을 만들어서 넣어줘야 할듯
        // 그래서 프론트에서 true면 신고하기가 안보이고, false면 신고하기 보이는 방식으로
        DiscussionCommentResponseDTO discussionCommentResponseDTO = reportService.reportComment(
            memberId, commentId, reportRequestDTO);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(discussionCommentResponseDTO));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<ReportResponseDTO>>> getReportList(
        @ModelAttribute ScrollRequestDTO requestDTO) {  // 신고에서는 lastId, size, sort, tabCondition만 사용

        ScrollResponseDTO<ReportResponseDTO> response = reportService.getReportList(requestDTO);

        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }

    @GetMapping("/{reportId}")
    public ResponseEntity<ApiResponseDTO<ReportResponseDTO>> getReportDetail(
        @PathVariable("reportId") Long reportId) {

        ReportResponseDTO result = reportService.getReportDetail(reportId);

        return ResponseEntity.ok(ApiResponseDTO.success(result));
    }

    @PatchMapping("/reject/{reportId}")
    public ResponseEntity<ApiResponseDTO<Void>> rejectReport(
        @PathVariable("reportId") Long reportId) {

        reportService.rejectReport(reportId);

        return ResponseEntity.ok(ApiResponseDTO.success(null));
    }

    @PatchMapping("/approval/{reportId}")
    public ResponseEntity<ApiResponseDTO<Void>> approvalReport(
        @PathVariable("reportId") Long reportId) {

        reportService.approvalReport(reportId);

        return ResponseEntity.ok(ApiResponseDTO.success(null));
    }

}
