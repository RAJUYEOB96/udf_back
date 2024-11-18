package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.response.social.MemberSocialInfoResponseDTO;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.FollowRepository;
import com.undefinedus.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class SocialServiceImpl implements SocialService{
    
    // === 에러 메시지 상수 === //
    private static final String USER_NOT_FOUND = "해당 유저를 찾을 수 없습니다. : %d";
    
    // === Repository 주입 === //
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    
    @Override
    public MemberSocialInfoResponseDTO getMemberSocialSimpleInfo(Long memberId) {
        
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(String.format(USER_NOT_FOUND, memberId)));
        
        // count를 셀때 N+1 문제가 발생할 수 있기 때문에 따로 찾아온거 넣어주기
        int followingCount = followRepository.countFollowingsByMemberId(memberId);
        int followerCount = followRepository.countFollowersByMemberId(memberId);
        
        return MemberSocialInfoResponseDTO.from(findMember, followingCount, followerCount);
    }
}
