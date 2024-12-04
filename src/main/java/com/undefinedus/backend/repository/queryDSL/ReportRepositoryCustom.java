package com.undefinedus.backend.repository.queryDSL;

import com.undefinedus.backend.domain.entity.Report;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import java.util.List;

public interface ReportRepositoryCustom {
    
    Long countReportListByTabCondition(ScrollRequestDTO requestDTO);
    
    List<Report> getReportListByTabCondition(ScrollRequestDTO requestDTO);
}
