package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.report.ReportRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.report.ReportResponseDTO;

public interface ReportService {

    void report(Long reporterId, ReportRequestDTO reportRequestDTO);
    
    ScrollResponseDTO<ReportResponseDTO> getReportList(ScrollRequestDTO requestDTO);
}
