package com.undefinedus.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.social.MemberSocialInfoResponseDTO;
import com.undefinedus.backend.dto.response.social.OtherMemberInfoResponseDTO;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.FollowRepository;
import com.undefinedus.backend.repository.MemberRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    
    @Test
    @DisplayName("다른 멤버 목록 조회 테스트 - 기본 케이스")
    void getOtherMembers_Success() {
        // given
        Long memberId = 1L;
        ScrollRequestDTO requestDTO = new ScrollRequestDTO();
        requestDTO.setSize(10);
        
        List<Member> mockMembers = createMockMembers(5); // 테스트용 멤버 5명 생성
        Set<Long> mockFollowingIds = Set.of(2L, 3L); // memberId 1이 2,3번 멤버를 팔로우
        
        given(memberRepository.findAllWithoutMemberId(memberId, requestDTO))
                .willReturn(mockMembers);
        given(followRepository.findFollowingIds(memberId))
                .willReturn(mockFollowingIds);
        
        // when
        ScrollResponseDTO<OtherMemberInfoResponseDTO> result = socialService.getOtherMembers(memberId, requestDTO);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getLastId()).isEqualTo(mockMembers.get(4).getId());
        assertThat(result.getNumberOfElements()).isEqualTo(5);
        
        // 팔로우 상태 검증
        result.getContent().forEach(dto -> {
            if (mockFollowingIds.contains(dto.getId())) {
                assertThat(dto.isFollowing()).isTrue();
            } else {
                assertThat(dto.isFollowing()).isFalse();
            }
        });
        
        verify(memberRepository).findAllWithoutMemberId(memberId, requestDTO);
        verify(followRepository).findFollowingIds(memberId);
    }
    
    @Test
    @DisplayName("다른 멤버 목록 조회 테스트 - 다음 페이지가 있는 경우")
    void getOtherMembers_WithNextPage() {
        // given
        Long memberId = 1L;
        ScrollRequestDTO requestDTO = new ScrollRequestDTO();
        requestDTO.setSize(10);
        
        List<Member> mockMembers = createMockMembers(11); // size + 1 개의 멤버 생성
        Set<Long> mockFollowingIds = Set.of(2L, 3L);
        
        given(memberRepository.findAllWithoutMemberId(memberId, requestDTO))
                .willReturn(mockMembers);
        given(followRepository.findFollowingIds(memberId))
                .willReturn(mockFollowingIds);
        
        // when
        ScrollResponseDTO<OtherMemberInfoResponseDTO> result = socialService.getOtherMembers(memberId, requestDTO);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(10); // 마지막 항목이 제거되어 10개만 반환
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getLastId()).isEqualTo(mockMembers.get(9).getId());
        assertThat(result.getNumberOfElements()).isEqualTo(10);
    }
    
    @Test
    @DisplayName("다른 멤버 목록 조회 테스트 - 빈 결과")
    void getOtherMembers_EmptyResult() {
        // given
        Long memberId = 1L;
        ScrollRequestDTO requestDTO = new ScrollRequestDTO();
        requestDTO.setSize(10);
        requestDTO.setLastId(100L);
        
        given(memberRepository.findAllWithoutMemberId(memberId, requestDTO))
                .willReturn(Collections.emptyList());
        given(followRepository.findFollowingIds(memberId))
                .willReturn(Collections.emptySet());
        
        // when
        ScrollResponseDTO<OtherMemberInfoResponseDTO> result = socialService.getOtherMembers(memberId, requestDTO);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getLastId()).isEqualTo(requestDTO.getLastId());
        assertThat(result.getNumberOfElements()).isZero();
    }
    
    // 테스트용 Mock Member 리스트 생성 헬퍼 메서드
    private List<Member> createMockMembers(int count) {
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            members.add(Member.builder()
                    .id((long) i)
                    .username("test" + i + "@example.com")
                    .nickname("Test User " + i)
                    .isPublic(true)
                    .preferences(Set.of(PreferencesType.과학)) // 필요한 경우 추가
                    .build());
        }
        return members;
    }
}