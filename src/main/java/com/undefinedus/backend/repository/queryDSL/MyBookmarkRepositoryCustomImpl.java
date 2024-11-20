package com.undefinedus.backend.repository.queryDSL;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.undefinedus.backend.domain.entity.MyBookmark;
import com.undefinedus.backend.domain.entity.QMyBookmark;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@Log4j2
@RequiredArgsConstructor
public class MyBookmarkRepositoryCustomImpl implements MyBookmarkRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<MyBookmark> findBookmarksWithScroll(Long memberId, ScrollRequestDTO requestDTO) {
        QMyBookmark myBookmark = QMyBookmark.myBookmark;
        
        // 기본 쿼리 생성
        // BooleanBuilder이란 QueryDSL에서 동적 쿼리를 생성할 때 사용하는 클래스입니다
        // 여러 조건들을 and()나 or()로 연결할 수 있게 해주는 빌더 패턴 구현체입니다
        BooleanBuilder builder = new BooleanBuilder();
        
        // Member 필터링 - 필수 조건
        builder.and(myBookmark.member.id.eq(memberId));
        
        // 검색어 처리 (책 제목과 구절 동시에 검색)
        if (StringUtils.hasText(requestDTO.getSearch())) {
            builder.and(myBookmark.aladinBook.title.containsIgnoreCase(requestDTO.getSearch())
                    .or(myBookmark.phrase.containsIgnoreCase(requestDTO.getSearch())));
        }
        
        // cursor는 마지막으로 로드된 항목의 기준값(여기서는 ID)을 의미합니다
        // 이전/다음 페이지로 이동할 때 offset을 사용하는 대신 마지막으로 본 항목의 ID를 기준으로 데이터를 가져옵니다
        // Cursor 조건 추가
        if (requestDTO.getLastId() > 0) {
            if ("desc".equals(requestDTO.getSort())) {
                builder.and(myBookmark.id.lt(requestDTO.getLastId()));
            } else {
                builder.and(myBookmark.id.gt(requestDTO.getLastId()));
            }
        }
        
        // 정렬 조건 설정
        OrderSpecifier<?> orderSpecifier = "desc".equals(requestDTO.getSort()) ?
                myBookmark.createdDate.desc() : myBookmark.createdDate.asc();
        
        // 요청한 크기보다 1개 더 가져오는 이유는 다음 페이지 존재 여부를 확인하기 위함입니다
        // 만약 size가 10이고 11개가 조회되면, 마지막 1개는 제거하고 hasNext를 true로 설정합니다
        return queryFactory
                .selectFrom(myBookmark)
                .leftJoin(myBookmark.aladinBook).fetchJoin()
                .leftJoin(myBookmark.member).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifier)
                .limit(requestDTO.getSize() + 1) // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
                .fetch();
    }
    
    @Override
    public Long countByMemberIdAndStatus(Long memberId, ScrollRequestDTO requestDTO) {
        QMyBookmark myBookmark = QMyBookmark.myBookmark;
        
        // 기본 쿼리 생성
        // BooleanBuilder이란 QueryDSL에서 동적 쿼리를 생성할 때 사용하는 클래스입니다
        // 여러 조건들을 and()나 or()로 연결할 수 있게 해주는 빌더 패턴 구현체입니다
        BooleanBuilder builder = new BooleanBuilder();
        
        // Member 필터링 - 필수 조건
        builder.and(myBookmark.member.id.eq(memberId));
        
        // 검색어 처리 (책 제목과 구절 동시에 검색)
        if (StringUtils.hasText(requestDTO.getSearch())) {
            builder.and(myBookmark.aladinBook.title.containsIgnoreCase(requestDTO.getSearch())
                    .or(myBookmark.phrase.containsIgnoreCase(requestDTO.getSearch())));
        }
        
        return queryFactory
                .select(myBookmark.count())
                .from(myBookmark)
                .where(builder)
                .fetchOne();
    }
}
