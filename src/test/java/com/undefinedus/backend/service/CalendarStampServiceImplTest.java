package com.undefinedus.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.undefinedus.backend.domain.entity.CalendarStamp;
import com.undefinedus.backend.dto.response.calendar.CalendarStampResponseDTO;
import com.undefinedus.backend.exception.calendar.YearOrMonthException;
import com.undefinedus.backend.repository.CalendarStampRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CalendarStampServiceImplTest {

    @Mock
    private CalendarStampRepository calendarStampRepository;

    @InjectMocks
    private CalendarStampServiceImpl calendarStampService;

    private List<CalendarStamp> calendarStamps;
    private Long memberId;
    
    
    @BeforeEach
    void setUp() {
        memberId = 1L;
        
        // 테스트에 사용할 CalendarStamp 데이터 생성
        calendarStamps = Arrays.asList(
                CalendarStamp.builder().recordedAt(LocalDate.of(2023, 5, 1)).build(),
                CalendarStamp.builder().recordedAt(LocalDate.of(2023, 5, 2)).build(),
                CalendarStamp.builder().recordedAt(LocalDate.of(2023, 5, 2)).build(),
                CalendarStamp.builder().recordedAt(LocalDate.of(2023, 6, 1)).build()
        );
    }
    
    @Test
    @DisplayName("특정 연도와 월에 해당하는 CalendarStamp 조회 테스트")
    void getAllStampsWhenYearAndMonth_shouldReturnGroupedStamps() {
        // given
        Integer year = 2023;
        Integer month = 5;
        
        when(calendarStampRepository.getAllStampsWhenYearAndMonth(memberId, year, month))
                .thenReturn(calendarStamps.subList(0, 3));
        
        // when
        Map<LocalDate, List<CalendarStampResponseDTO>> result =
                calendarStampService.getAllStampsWhenYearAndMonth(memberId, year, month);
        
        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(LocalDate.of(2023, 5, 1)));
        assertTrue(result.containsKey(LocalDate.of(2023, 5, 2)));
        assertEquals(1, result.get(LocalDate.of(2023, 5, 1)).size());
        assertEquals(2, result.get(LocalDate.of(2023, 5, 2)).size());
    }
    
    @Test
    @DisplayName("특정 연도와 월에 해당하는 CalendarStamp이 없을 경우 빈 Map 반환 테스트")
    void getAllStampsWhenYearAndMonth_shouldReturnEmptyMapWhenNoStamps() {
        // given
        Integer year = 2023;
        Integer month = 7;
        
        when(calendarStampRepository.getAllStampsWhenYearAndMonth(memberId, year, month))
                .thenReturn(Arrays.asList());
        
        // when
        Map<LocalDate, List<CalendarStampResponseDTO>> result =
                calendarStampService.getAllStampsWhenYearAndMonth(memberId, year, month);
        
        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    @DisplayName("year 또는 month가 null일 경우 예외 발생 테스트")
    void getAllStampsWhenYearAndMonth_shouldThrowExceptionWhenYearOrMonthIsNull() {
        // given
        Integer year = null;
        Integer month = null;
        
        // when & then
        assertThrows(YearOrMonthException.class, () ->
                calendarStampService.getAllStampsWhenYearAndMonth(memberId, year, month));
    }
}