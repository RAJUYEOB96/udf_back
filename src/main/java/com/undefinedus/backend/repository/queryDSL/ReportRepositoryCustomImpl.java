package com.undefinedus.backend.repository.queryDSL;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.undefinedus.backend.domain.entity.QReport;
import com.undefinedus.backend.domain.entity.Report;
import com.undefinedus.backend.domain.enums.ReportStatus;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.exception.social.TabConditionNotEqualException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
@RequiredArgsConstructor
public class ReportRepositoryCustomImpl implements ReportRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    
    @Override
    public Long countReportListByTabCondition(ScrollRequestDTO requestDTO) {
        QReport report = QReport.report;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        if ("미처리".equals(requestDTO.getTabCondition())) {
            // 미처리된 것들만 가져오기 위해
            builder.and(report.status.eq(ReportStatus.PENDING).or(report.status.eq(ReportStatus.TEMPORARY_ACCEPTED)));
        } else if ("처리 완료".equals(requestDTO.getTabCondition())) {
            // 처리된 것들만 가져오기 위해
            builder.and(report.status.eq(ReportStatus.ACCEPTED).or(report.status.eq(ReportStatus.REJECTED)));
        } else {
            throw new TabConditionNotEqualException("해당 tabCondition의 값이 올바르지 않습니다. : " + requestDTO.getTabCondition());
        }
        
        return queryFactory
                .select(report.count())
                .from(report)
                .where(builder)
                .fetchOne();
    }
    
    @Override
    public List<Report> getReportListByTabCondition(ScrollRequestDTO requestDTO) {
        QReport report = QReport.report;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        if ("미처리".equals(requestDTO.getTabCondition())) {
            // 미처리된 것들만 가져오기 위해
            builder.and(report.status.eq(ReportStatus.PENDING).or(report.status.eq(ReportStatus.TEMPORARY_ACCEPTED)));
        } else if ("처리 완료".equals(requestDTO.getTabCondition())) {
            // 처리된 것들만 가져오기 위해
            builder.and(report.status.eq(ReportStatus.ACCEPTED).or(report.status.eq(ReportStatus.REJECTED)));
        } else {
            throw new TabConditionNotEqualException("해당 tabCondition의 값이 올바르지 않습니다. : " + requestDTO.getTabCondition());
        }
        // cursor는 마지막으로 로드된 항목의 기준값(여기서는 ID)을 의미합니다
        // 이전/다음 페이지로 이동할 때 offset을 사용하는 대신 마지막으로 본 항목의 ID를 기준으로 데이터를 가져옵니다
        // Cursor 조건 추가
        if (requestDTO.getLastId() > 0) {
            if ("desc".equals(requestDTO.getSort())) {
                builder.and(report.id.lt(requestDTO.getLastId()));
            } else {
                builder.and(report.id.gt(requestDTO.getLastId()));
            }
        }
        
        // 정렬 조건 설정
        OrderSpecifier<?> orderSpecifier = "desc".equals(requestDTO.getSort()) ?
                report.createdDate.desc() : report.createdDate.asc();
        
        return queryFactory
                .selectFrom(report)
                .leftJoin(report.reporter).fetchJoin()
                .leftJoin(report.reported).fetchJoin()
                .leftJoin(report.discussion).fetchJoin()
                .leftJoin(report.comment).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifier)
                .limit(requestDTO.getSize() + 1) // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
                .fetch();
    }
}
