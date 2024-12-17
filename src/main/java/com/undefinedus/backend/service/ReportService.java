package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.report.ReportRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.discussionComment.DiscussionCommentResponseDTO;
import com.undefinedus.backend.dto.response.report.ReportResponseDTO;

public interface ReportService {

    void reportDiscussion(Long reporterId, Long discussionId,
        ReportRequestDTO reportRequestDTO);

    DiscussionCommentResponseDTO reportComment(Long reporterId, Long commentId, ReportRequestDTO reportRequestDTO);

    ScrollResponseDTO<ReportResponseDTO> getReportList(ScrollRequestDTO requestDTO);
    
    ReportResponseDTO getReportDetail(Long reportId);
    
    void rejectReport(Long reportId);
    
    void approvalReport(Long reportId);
}
