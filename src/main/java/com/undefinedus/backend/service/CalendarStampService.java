package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.response.calendar.CalendarStampResponseDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CalendarStampService {
    
    Map<LocalDate, List<CalendarStampResponseDTO>> getAllStampsWhenYearAndMonth(Long memberId, Integer year,
            Integer month);
}
