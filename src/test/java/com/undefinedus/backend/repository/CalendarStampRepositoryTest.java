package com.undefinedus.backend.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.undefinedus.backend.domain.entity.CalendarStamp;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Log4j2
class CalendarStampRepositoryTest {

    @Autowired
    private CalendarStampRepository calendarStampRepository;

    @Test
    @DisplayName("CalendarStampRepository 연결 확인 테스트")
    void checkConnections() {
        assertNotNull(calendarStampRepository, "CalendarStampRepository가 주입되지 않았습니다.");
    }

}