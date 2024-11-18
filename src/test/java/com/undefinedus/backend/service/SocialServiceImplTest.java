package com.undefinedus.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.response.social.MemberSocialInfoResponseDTO;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.FollowRepository;
import com.undefinedus.backend.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SocialServiceImplTest {
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private FollowRepository followRepository;
    
    @InjectMocks
    private SocialServiceImpl socialService;
    
    @Test
    @DisplayName("회원 ID로 소셜 정보 조회 성공 테스트")
    void getMemberSocialSimpleInfo_Success() {
        // given
        Long memberId = 1L;
        Member mockMember = Member.builder()
                .id(memberId)
                .username("test@example.com")
                .nickname("Test User")
                .isPublic(true)
                .build();
        
        given(memberRepository.findById(memberId))
                .willReturn(Optional.of(mockMember));
        
        // FollowRepository 동작 정의
        given(followRepository.countFollowingsByMemberId(memberId))
                .willReturn(5); // 팔로잉 5명
        given(followRepository.countFollowersByMemberId(memberId))
                .willReturn(3); // 팔로워 3명
        
        // when
        MemberSocialInfoResponseDTO result = socialService.getMemberSocialSimpleInfo(memberId);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getNickname()).isEqualTo(mockMember.getNickname());
        assertThat(result.getFollowingCount()).isEqualTo(5);
        assertThat(result.getFollowerCount()).isEqualTo(3);
        
        verify(memberRepository).findById(memberId);
        verify(followRepository).countFollowingsByMemberId(memberId);
        verify(followRepository).countFollowersByMemberId(memberId);
    }
    
    @Test
    @DisplayName("존재하지 않는 회원 ID로 조회 시 예외 발생 테스트")
    void getMemberSocialSimpleInfo_UserNotFound() {
        // given
        Long nonExistentMemberId = 999L;
        given(memberRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        
        // when & then
        MemberNotFoundException exception = assertThrows(
                MemberNotFoundException.class,
                () -> socialService.getMemberSocialSimpleInfo(nonExistentMemberId)
        );
        
        assertThat(exception.getMessage())
                .contains(String.format("해당 유저를 찾을 수 없습니다. : %d", nonExistentMemberId));
        verify(memberRepository).findById(nonExistentMemberId);
    }
    
    @Test
    @DisplayName("팔로우 관계가 없는 회원 조회 테스트")
    void getMemberSocialSimpleInfo_NoFollows() {
        // given
        Long memberId = 1L;
        Member mockMember = Member.builder()
                .id(memberId)
                .username("test@example.com")
                .nickname("Test User")
                .isPublic(true)
                .build();
        
        given(memberRepository.findById(memberId))
                .willReturn(Optional.of(mockMember));
        
        given(followRepository.countFollowingsByMemberId(memberId))
                .willReturn(0);
        given(followRepository.countFollowersByMemberId(memberId))
                .willReturn(0);
        
        // when
        MemberSocialInfoResponseDTO result = socialService.getMemberSocialSimpleInfo(memberId);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getFollowingCount()).isZero();
        assertThat(result.getFollowerCount()).isZero();
        
        verify(memberRepository).findById(memberId);
        verify(followRepository).countFollowingsByMemberId(memberId);
        verify(followRepository).countFollowersByMemberId(memberId);
    }
}