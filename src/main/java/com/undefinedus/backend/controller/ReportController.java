package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.report.ReportRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.report.ReportResponseDTO;
import com.undefinedus.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/reportDiscussionAndComment")
    public ResponseEntity<ApiResponseDTO<Void>> report(@AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestBody ReportRequestDTO reportRequestDTO
        ) {

        Long memberId = memberSecurityDTO.getId();

        reportService.report(memberId, reportRequestDTO);

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(null));
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

}
