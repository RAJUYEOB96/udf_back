package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.request.report.ReportRequestDTO;

public interface ReportService {

    void report(Long reporterId, ReportRequestDTO reportRequestDTO);
}
