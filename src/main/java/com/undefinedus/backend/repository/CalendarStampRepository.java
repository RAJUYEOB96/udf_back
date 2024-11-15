package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.CalendarStamp;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarStampRepository extends JpaRepository<CalendarStamp, Long> {
    
    Integer countByMemberIdAndMyBookId(Long memberId, Long id);
}
