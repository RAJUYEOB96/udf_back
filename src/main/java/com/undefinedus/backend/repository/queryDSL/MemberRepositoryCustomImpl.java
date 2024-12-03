package com.undefinedus.backend.repository.queryDSL;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.QFollow;
import com.undefinedus.backend.domain.entity.QMember;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.domain.entity.QMyBookmark;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.exception.social.TabConditionNotEqualException;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@Log4j2
@RequiredArgsConstructor
public class MemberRepositoryCustomImpl implements MemberRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    private final EntityManager em;  // MemberRepository 대신 EntityManager 사용
    @Override
    public List<Member> findAllWithoutMemberId(Long memberId, ScrollRequestDTO requestDTO) {
        QMember member = QMember.member;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // memberId 와 일치 하지 않는 것만 들고 오기 위해 ne (not equal)
        builder.and(member.id.ne(memberId));
        
        // ADMIN role을 가지고 있지 않는 것만 들고 오기
        builder.and(member.memberRoleList.contains(MemberType.ADMIN).not());

        // 검색어 처리 (닉네임 검색)
        if (StringUtils.hasText(requestDTO.getSearch())) {
            // 검색어로 시작하는 닉네임들만 가져오기
            builder.and(member.nickname.startsWith(requestDTO.getSearch()));
        }
        
        // cursor는 마지막으로 로드된 항목의 기준값(여기서는 ID)을 의미합니다
        // 이전/다음 페이지로 이동할 때 offset을 사용하는 대신 마지막으로 본 항목의 ID를 기준으로 데이터를 가져옵니다
        // Cursor 조건 추가
        // lastId로 Member 조회할 때 MemberRepository 대신 EntityManager 사용
        if (requestDTO.getLastId() > 0) {
            // 마지막으로 본 멤버 정보 조회
            Member lastMember = em.find(Member.class, requestDTO.getLastId());
            
            if (lastMember != null) {
                // 닉네임이 더 크거나
                // 닉네임이 같다면 ID가 더 큰 데이터 조회
                builder.and(
                        member.nickname.gt(lastMember.getNickname())
                                .or(
                                        member.nickname.eq(lastMember.getNickname())
                                                .and(member.id.gt(lastMember.getId()))
                                )
                );
            }
        }
        
        // 정렬 조건 설정 (닉네임 검색은 무조건 사전순으로)
        OrderSpecifier<?> orderSpecifier = member.nickname.asc();
        
        return queryFactory
                .selectFrom(member)
                .where(builder)
                .orderBy(
                        member.nickname.asc(), // 닉네임 오름차순 정렬
                        member.id.asc()        // 같은 닉네임일 경우 ID 오름차순
                )
                .limit(requestDTO.getSize() + 1) // 다음 페이지 존재 여부 확인을 위해 1개 더 조회
                .fetch();
    }
    
    @Override
    public Long countAllWithoutMemberId(Long memberId, ScrollRequestDTO requestDTO) {
        QMember member = QMember.member;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        // memberId 와 일치 하지 않는 것만 들고 오기 위해 ne (not equal)
        builder.and(member.id.ne(memberId));
        
        // ADMIN role을 가지고 있지 않는 것만 들고 오기
        builder.and(member.memberRoleList.contains(MemberType.ADMIN).not());

        // 검색어 처리 (닉네임 검색)
        if (StringUtils.hasText(requestDTO.getSearch())) {
            // 검색어로 시작하는 닉네임들만 가져오기
            builder.and(member.nickname.startsWith(requestDTO.getSearch()));
        }
        
        return queryFactory
                .select(member.count())
                .from(member)
                .where(builder)
                .fetchOne();
    }
    
    @Override
    public List<Member> findFollowMembersByTabCondition(Long memberId, ScrollRequestDTO requestDTO) {
        QFollow follow = QFollow.follow;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        if ("팔로워".equals(requestDTO.getTabCondition())) {
            // 나를 팔로우하는 사람들 조회 (팔로워)
            builder.and(follow.following.id.eq(memberId));
            
            // 검색어 처리 (팔로워 닉네임 검색)
            if (StringUtils.hasText(requestDTO.getSearch())) {
                builder.and(follow.follower.nickname.startsWith(requestDTO.getSearch()));
            }
            
            // Cursor 조건 추가
            if (requestDTO.getLastId() > 0) {
                Member lastMember = em.find(Member.class, requestDTO.getLastId());
                
                if (lastMember != null) {
                    builder.and(
                            follow.follower.nickname.gt(lastMember.getNickname())
                                    .or(
                                            follow.follower.nickname.eq(lastMember.getNickname())
                                                    .and(follow.follower.id.gt(lastMember.getId()))
                                    )
                    );
                }
            }
            
            return queryFactory
                    .select(follow.follower)
                    .from(follow)
                    .where(builder)
                    .orderBy(
                            follow.follower.nickname.asc(),
                            follow.follower.id.asc()
                    )
                    .limit(requestDTO.getSize() + 1)
                    .fetch();
        } else if ("팔로잉".equals(requestDTO.getTabCondition())) {
            // 내가 팔로우하는 사람들 조회 (팔로잉)
            builder.and(follow.follower.id.eq(memberId));
            
            // 검색어 처리 (팔로잉 닉네임 검색)
            if (StringUtils.hasText(requestDTO.getSearch())) {
                builder.and(follow.following.nickname.startsWith(requestDTO.getSearch()));
            }
            
            // Cursor 조건 추가
            if (requestDTO.getLastId() > 0) {
                Member lastMember = em.find(Member.class, requestDTO.getLastId());
                
                if (lastMember != null) {
                    builder.and(
                            follow.following.nickname.gt(lastMember.getNickname())
                                    .or(
                                            follow.following.nickname.eq(lastMember.getNickname())
                                                    .and(follow.following.id.gt(lastMember.getId()))
                                    )
                    );
                }
            }
            
            return queryFactory
                    .select(follow.following)
                    .from(follow)
                    .where(builder)
                    .orderBy(
                            follow.following.nickname.asc(),    // 여기서 정렬해서 가져옴
                            follow.following.id.asc()
                    )
                    .limit(requestDTO.getSize() + 1)
                    .fetch();
        } else {
            throw new TabConditionNotEqualException("해당 TabCondition이 일치하지 않습니다. : " + requestDTO.getTabCondition());
        }
    }
    
    @Override
    public Long countAllFollowMembersByTabCondition(Long memberId, ScrollRequestDTO requestDTO) {
        QFollow follow = QFollow.follow;
        
        BooleanBuilder builder = new BooleanBuilder();
        
        if ("팔로워".equals(requestDTO.getTabCondition())) {
            // 나를 팔로우하는 사람들 조회 (팔로워)
            builder.and(follow.following.id.eq(memberId));
            
            // 검색어 처리 (팔로워 닉네임 검색)
            if (StringUtils.hasText(requestDTO.getSearch())) {
                builder.and(follow.follower.nickname.startsWith(requestDTO.getSearch()));
            }
         
            return queryFactory
                    .select(follow.count())
                    .from(follow)
                    .where(builder)
                    .fetchOne();
        } else if ("팔로잉".equals(requestDTO.getTabCondition())) {
            // 내가 팔로우하는 사람들 조회 (팔로잉)
            builder.and(follow.follower.id.eq(memberId));
            
            // 검색어 처리 (팔로잉 닉네임 검색)
            if (StringUtils.hasText(requestDTO.getSearch())) {
                builder.and(follow.following.nickname.startsWith(requestDTO.getSearch()));
            }
            
            return queryFactory
                    .select(follow.count())
                    .from(follow)
                    .where(builder)
                    .fetchOne();
        } else {
            throw new TabConditionNotEqualException("해당 TabCondition이 일치하지 않습니다. : " + requestDTO.getTabCondition());
        }    }


    // 모든 회원 중 isMessageToKakao = true인 회원들의 id를 가져옴
    public List<Long> findMessageToKakaoMemberIdList() {
        QMember member = QMember.member;

        BooleanBuilder builder = new BooleanBuilder();

        builder.and(member.isMessageToKakao.eq(true));


        return queryFactory
            .select(member.id).distinct()
            .from(member)
            .join(myBookmark).on(myBookmark.member.eq(member))
            .where(member.isMessageToKakao.eq(true)
                .and(myBookmark.phrase.isNotNull())
                .and(myBookmark.phrase.ne("")))
            .fetch();
    }

}
