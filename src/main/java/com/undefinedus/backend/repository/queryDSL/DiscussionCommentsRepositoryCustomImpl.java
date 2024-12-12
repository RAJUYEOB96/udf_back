package com.undefinedus.backend.repository.queryDSL;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.undefinedus.backend.domain.entity.DiscussionComment;
import com.undefinedus.backend.domain.entity.QDiscussionComment;
import com.undefinedus.backend.dto.request.discussionComment.DiscussionCommentsScrollRequestDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

@Repository
@Log4j2
@RequiredArgsConstructor
public class DiscussionCommentsRepositoryCustomImpl implements DiscussionCommentsRepositoryCustom{
    private final JPAQueryFactory queryFactory;
    
    public List<DiscussionComment> findDiscussionCommentListWithScroll(
        DiscussionCommentsScrollRequestDTO requestDTO) {
        QDiscussionComment qDiscussionComment = QDiscussionComment.discussionComment;
        
        // 기본 쿼리 생성
        // BooleanBuilder이란 QueryDSL에서 동적 쿼리를 생성할 때 사용하는 클래스입니다
        // 여러 조건들을 and()나 or()로 연결할 수 있게 해주는 빌더 패턴 구현체입니다
        BooleanBuilder builder = new BooleanBuilder();

        // 같은 discussionId인 것만 가져오기
        builder.and(qDiscussionComment.discussion.id.eq(requestDTO.getDiscussionId()));

        // cursor는 마지막으로 로드된 항목의 기준값(여기서는 ID)을 의미합니다
        // 이전/다음 페이지로 이동할 때 offset을 사용하는 대신 마지막으로 본 항목의 ID를 기준으로 데이터를 가져옵니다
        // Cursor 조건 추가
        if (requestDTO.getLastId() > 0) {
            if ("asc".equals(requestDTO.getSort())) {
                builder.and(qDiscussionComment.id.lt(requestDTO.getLastId()));
            } else {
                builder.and(qDiscussionComment.id.gt(requestDTO.getLastId()));
            }
        }
        
        // 정렬 조건 설정
        OrderSpecifier<?> orderSpecifier = "desc".equals(requestDTO.getSort()) ?
            qDiscussionComment.totalOrder.desc() : qDiscussionComment.totalOrder.asc();
        
        // 요청한 크기보다 1개 더 가져오는 이유는 다음 페이지 존재 여부를 확인하기 위함입니다
        // 만약 size가 10이고 11개가 조회되면, 마지막 1개는 제거하고 hasNext를 true로 설정합니다
        return queryFactory
                .selectFrom(qDiscussionComment)
                .where(builder)
                .orderBy(orderSpecifier)
                .limit(requestDTO.getSize() + 1) // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
                .fetch();
    }
}
