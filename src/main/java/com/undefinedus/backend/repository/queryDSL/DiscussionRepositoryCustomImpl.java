package com.undefinedus.backend.repository.queryDSL;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.QDiscussion;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionScrollRequestDTO;
import com.undefinedus.backend.exception.book.InvalidStatusException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@Log4j2
@RequiredArgsConstructor
public class DiscussionRepositoryCustomImpl implements DiscussionRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    
    public List<Discussion> findDiscussionsWithScroll(DiscussionScrollRequestDTO requestDTO) {
        QDiscussion qDiscussion = QDiscussion.discussion;
        
        // 기본 쿼리 생성
        // BooleanBuilder이란 QueryDSL에서 동적 쿼리를 생성할 때 사용하는 클래스입니다
        // 여러 조건들을 and()나 or()로 연결할 수 있게 해주는 빌더 패턴 구현체입니다
        BooleanBuilder builder = new BooleanBuilder();

        // StringUtils이란 Spring Framework에서 제공하는 유틸리티 메서드입니다
        // 문자열이 null이 아니고, 길이가 0보다 크며, 공백이 아닌 문자를 하나 이상 포함하는지 확인합니다
        // str != null && str.trim().length() > 0와 같은 효과입니다
        // 읽기 상태(탭) 필터
        if (StringUtils.hasText(requestDTO.getStatus())) {
            try {
                builder.and(qDiscussion.status.eq(DiscussionStatus.valueOf(requestDTO.getStatus())));
            } catch (IllegalArgumentException e) {
                log.error("Invalid status value: {}", requestDTO.getStatus());
                throw new InvalidStatusException("유효하지 않은 토론 상태입니다: " + requestDTO.getStatus());
            }
        }
        
        // 검색어 처리 (글 제목과 책 제목 동시에 검색)
        if (StringUtils.hasText(requestDTO.getSearch())) {
            builder.and(qDiscussion.myBook.aladinBook.title.containsIgnoreCase(requestDTO.getSearch())
                    .or(qDiscussion.title.containsIgnoreCase(requestDTO.getSearch())));
        }
        
        // cursor는 마지막으로 로드된 항목의 기준값(여기서는 ID)을 의미합니다
        // 이전/다음 페이지로 이동할 때 offset을 사용하는 대신 마지막으로 본 항목의 ID를 기준으로 데이터를 가져옵니다
        // Cursor 조건 추가
        if (requestDTO.getLastId() > 0) {
            if ("desc".equals(requestDTO.getSort())) {
                builder.and(qDiscussion.id.lt(requestDTO.getLastId()));
            } else {
                builder.and(qDiscussion.id.gt(requestDTO.getLastId()));
            }
        }
        
        // 정렬 조건 설정
        OrderSpecifier<?> orderSpecifier = "desc".equals(requestDTO.getSort()) ?
            qDiscussion.createdDate.desc() : qDiscussion.createdDate.asc();
        
        // 요청한 크기보다 1개 더 가져오는 이유는 다음 페이지 존재 여부를 확인하기 위함입니다
        // 만약 size가 10이고 11개가 조회되면, 마지막 1개는 제거하고 hasNext를 true로 설정합니다
        return queryFactory
                .selectFrom(qDiscussion)
//                .leftJoin(qDiscussion.myBook.aladinBook).fetchJoin() // N+1 문제 방지를 위한 fetch join
                .where(builder)
                .orderBy(orderSpecifier)
                .limit(requestDTO.getSize() + 1) // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
                .fetch();
    }
}
