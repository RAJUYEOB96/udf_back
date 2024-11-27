package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.CalendarStamp;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CalendarStampRepository extends JpaRepository<CalendarStamp, Long> {
    
    Integer countByMemberIdAndMyBookId(Long memberId, Long id);
    
    void deleteAllByMyBookId(Long bookId);
    
    @Query("SELECT cs FROM CalendarStamp cs "
            + "WHERE YEAR(cs.recordedAt) = :year "
            + "AND MONTH(cs.recordedAt) = :month "
            + "AND cs.member.id = :memberId "
            + "ORDER BY cs.recordedAt")
    List<CalendarStamp> getAllStampsWhenYearAndMonth(@Param("memberId") Long memberId, @Param("year") Integer year,
            @Param("month") Integer month);
}
