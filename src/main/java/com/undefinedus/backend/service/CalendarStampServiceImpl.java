package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.CalendarStamp;
import com.undefinedus.backend.dto.response.calendar.CalendarStampResponseDTO;
import com.undefinedus.backend.exception.calendar.YearOrMonthException;
import com.undefinedus.backend.repository.CalendarStampRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class CalendarStampServiceImpl implements CalendarStampService{
    
    private static final String YEAR_OR_MONTH_ERROR = "해당 년 또는 월이 잘못 입력되었습니다. : year - %d, month - %d";
    
    private final CalendarStampRepository calendarStampRepository;
    
    
    @Override
    public Map<LocalDate, List<CalendarStampResponseDTO>> getAllStampsWhenYearAndMonth(Long memberId, Integer year,
            Integer month) {
        if (year == null || month == null) {
            throw new YearOrMonthException(String.format(YEAR_OR_MONTH_ERROR,year,month));
        }
        
        List<CalendarStamp> findList = calendarStampRepository.getAllStampsWhenYearAndMonth(memberId, year, month);
        
        for (CalendarStamp calendarStamp : findList) {
            log.info("calendarStamp : " + calendarStamp);
        }
        
        if (findList.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<LocalDate, List<CalendarStampResponseDTO>> stampsByDate = new HashMap<>();
        
        for (CalendarStamp stamp : findList) {
            CalendarStampResponseDTO responseDTO = CalendarStampResponseDTO.from(stamp);
            
            LocalDate date = stamp.getRecordedAt();
            
            if (!stampsByDate.containsKey(date)) {
                stampsByDate.put(date, new ArrayList<>());
            }
            stampsByDate.get(date).add(responseDTO);
        }
        
        return stampsByDate;
    }
}
