package com.undefinedus.backend.controller;

import com.undefinedus.backend.domain.entity.CalendarStamp;
import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.calendar.CalendarStampResponseDTO;
import com.undefinedus.backend.service.CalendarStampService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/calendar")
public class CalendarStampController {
    
    private final CalendarStampService calendarStampService;
    
    @GetMapping("/getStamps")
    public ResponseEntity<ApiResponseDTO<Map<LocalDate, List<CalendarStampResponseDTO>>>> getStamps(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @RequestParam("year") Integer year, @RequestParam("month") Integer month) {
        
        Long memberId = memberSecurityDTO.getId();
        
        Map<LocalDate, List<CalendarStampResponseDTO>> result = calendarStampService.getAllStampsWhenYearAndMonth(
                memberId, year, month);
        return ResponseEntity.ok(ApiResponseDTO.success(result));
    }
}
