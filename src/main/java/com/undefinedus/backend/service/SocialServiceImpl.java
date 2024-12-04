package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.Follow;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.social.MemberSocialInfoResponseDTO;
import com.undefinedus.backend.dto.response.social.OtherMemberInfoResponseDTO;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.exception.social.InvalidFollowException;
import com.undefinedus.backend.repository.FollowRepository;
import com.undefinedus.backend.repository.MemberRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class SocialServiceImpl implements SocialService {

    // === 에러 메시지 상수 === //
    private static final String USER_NOT_FOUND = "해당 유저를 찾을 수 없습니다. : %d";

    // === Repository 주입 === //
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    @Override
    public MemberSocialInfoResponseDTO getMemberSocialSimpleInfo(Long memberId) {

        Member findMember = memberRepository.findById(memberId)
            .orElseThrow(
                () -> new MemberNotFoundException(String.format(USER_NOT_FOUND, memberId)));

        // count를 셀때 N+1 문제가 발생할 수 있기 때문에 따로 찾아온거 넣어주기
        int followingCount = followRepository.countFollowingsByMemberId(memberId);
        int followerCount = followRepository.countFollowersByMemberId(memberId);

        return MemberSocialInfoResponseDTO.from(findMember, followingCount, followerCount);
    }

    @Override
    public ScrollResponseDTO<OtherMemberInfoResponseDTO> getOtherMembers(Long memberId,
        ScrollRequestDTO requestDTO) {

        Long totalElements = memberRepository.countAllWithoutMemberId(memberId, requestDTO);

        List<Member> otherMembers = memberRepository.findAllWithoutMemberId(memberId, requestDTO);

        boolean hasNext = false;
        if (otherMembers.size() > requestDTO.getSize()) { // 11 > 10 이면 있다는 뜻
            hasNext = true;
            otherMembers.remove(otherMembers.size() - 1); // 11개 가져온 걸 10개를 보내기 위해
        }

        // 팔로우 정보 조회 (현재 로그인한 memberId가 팔로우 하는 모든 ID 목록)
        // 내가 팔로잉 (내가 남을) 하고 있는 모든 memberId
        Set<Long> followingIds = followRepository.findFollowingIds(memberId);

        List<OtherMemberInfoResponseDTO> dtoList = otherMembers.stream()
            .map(otherMember -> {
                return OtherMemberInfoResponseDTO.from(otherMember, followingIds);
            }).collect(Collectors.toList());

        // 마지막 항목의 ID 설정
        Long lastId = otherMembers.isEmpty() ?
            requestDTO.getLastId() :    // 조회된 목록이 비어있는 경우를 대비해 삼항 연산자 사용
            otherMembers.get(otherMembers.size() - 1)
                .getId(); // lastId를 요청 DTO의 값이 아닌, 실제 조회된 마지막 항목의 ID로 설정

        String lastNickname = otherMembers.isEmpty() ? requestDTO.getLastNickname() :
            otherMembers.get(otherMembers.size() - 1).getNickname();

        return ScrollResponseDTO.<OtherMemberInfoResponseDTO>withAll()
            .content(dtoList)
            .hasNext(hasNext)
            .lastId(lastId)
            .lastNickname(lastNickname)  // lastNickname 필드도 추가
            .numberOfElements(dtoList.size())
            .totalElements(totalElements)
            .build();
    }

    @Override
    public ScrollResponseDTO<OtherMemberInfoResponseDTO> getMemberFollows(Long memberId,
        ScrollRequestDTO requestDTO) {

        Long totalElements = memberRepository.countAllFollowMembersByTabCondition(memberId,
            requestDTO);

        List<Member> followMembers = memberRepository.findFollowMembersByTabCondition(memberId,
            requestDTO);

        boolean hasNext = false;
        if (followMembers.size() > requestDTO.getSize()) { // 11 > 10 이면 있다는 뜻
            hasNext = true;
            followMembers.remove(followMembers.size() - 1); // 11개 가져온 걸 10개를 보내기 위해
        }

        // 팔로우 정보 조회 (현재 로그인한 memberId가 팔로우 하는 모든 ID 목록)
        // 내가 팔로잉 (내가 남을) 하고 있는 모든 memberId
        Set<Long> followingIds = followRepository.findFollowingIds(memberId);

        List<OtherMemberInfoResponseDTO> dtoList = followMembers.stream()
            .map(followMember -> {
                return OtherMemberInfoResponseDTO.from(followMember, followingIds);
            }).collect(Collectors.toList());

        // 마지막 항목의 ID 설정
        Long lastId = followMembers.isEmpty() ?
            requestDTO.getLastId() :    // 조회된 목록이 비어있는 경우를 대비해 삼항 연산자 사용
            followMembers.get(followMembers.size() - 1)
                .getId(); // lastId를 요청 DTO의 값이 아닌, 실제 조회된 마지막 항목의 ID로 설정

        String lastNickname = followMembers.isEmpty() ? requestDTO.getLastNickname() :
            followMembers.get(followMembers.size() - 1).getNickname();

        return ScrollResponseDTO.<OtherMemberInfoResponseDTO>withAll()
            .content(dtoList)
            .hasNext(hasNext)
            .lastId(lastId)
            .lastNickname(lastNickname)  // lastNickname 필드도 추가
            .numberOfElements(dtoList.size())
            .totalElements(totalElements)
            .build();
    }

    @Override
    public void toggleFollowStatus(Long myMemberId, Long targetMemberId) {

        // 1. 자기 자신을 팔로우하는 경우 예외 처리
        if (myMemberId.equals(targetMemberId)) {
            throw new InvalidFollowException("자신과 동일한 아이디를 팔로우 할 수 없습니다.");
        }

        // 2. 팔로우할 대상 회원과 자신의 정보를 조회
        Member follower = memberRepository.findById(myMemberId)
            .orElseThrow(
                () -> new MemberNotFoundException(String.format(USER_NOT_FOUND, myMemberId)));
        Member following = memberRepository.findById(targetMemberId)
            .orElseThrow(
                () -> new MemberNotFoundException(String.format(USER_NOT_FOUND, targetMemberId)));

        // 3. 이미 팔로우 관계가 있는지 확인
        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowing(follower,
            following);

        if (existingFollow.isPresent()) {
            // 4-1. 팔로우 관계가 있으면 언팔로우
            followRepository.delete(existingFollow.get());  // 이건 Follow에 있던거 지우는 것
        } else {
            // 4-2 팔로우 관계가 없으면 팔로우 생성
            Follow newFollow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

            followRepository.save(newFollow);           // Follow에 새로운 관계 추가
        }
    }

    @Override
    public MemberSocialInfoResponseDTO getOtherMemberSocialSimpleInfo(Long myMemberId,
        Long targetMemberId) {

        Member findMyMember = memberRepository.findById(myMemberId)
            .orElseThrow(
                () -> new MemberNotFoundException(String.format(USER_NOT_FOUND, myMemberId)));

        Member findTargetMember = memberRepository.findById(targetMemberId)
            .orElseThrow(
                () -> new MemberNotFoundException(String.format(USER_NOT_FOUND, targetMemberId)));

        // 이미 팔로우 관계가 있는지 확인
        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowing(findMyMember,
            findTargetMember);
        Boolean isFollow = false;

        if (existingFollow.isPresent()) {
            isFollow = true;
        }

        // count를 셀때 N+1 문제가 발생할 수 있기 때문에 따로 찾아온거 넣어주기
        int followingCount = followRepository.countFollowingsByMemberId(targetMemberId);
        int followerCount = followRepository.countFollowersByMemberId(targetMemberId);

        return MemberSocialInfoResponseDTO.from(findTargetMember, followingCount, followerCount,
            isFollow);
    }

    @Override
    public ScrollResponseDTO<OtherMemberInfoResponseDTO> getOtherMemberFollows(Long loginMemberId,
        Long targetMemberId,
        ScrollRequestDTO requestDTO) {

        Long totalElements = memberRepository.countAllFollowMembersByTabCondition(targetMemberId,
            requestDTO);

        List<Member> followMembers = memberRepository.findFollowMembersByTabCondition(
            targetMemberId, requestDTO);

        boolean hasNext = false;
        if (followMembers.size() > requestDTO.getSize()) { // 11 > 10 이면 있다는 뜻
            hasNext = true;
            followMembers.remove(followMembers.size() - 1); // 11개 가져온 걸 10개를 보내기 위해
        }

        // 팔로우 정보 조회 (현재 로그인한 memberId가 팔로우 하는 모든 ID 목록)
        // 내가 팔로잉 (내가 남을) 하고 있는 모든 memberId
        Set<Long> followingIds = followRepository.findFollowingIds(loginMemberId);

        List<OtherMemberInfoResponseDTO> dtoList = followMembers.stream()
            .map(followMember -> {
                return OtherMemberInfoResponseDTO.from(followMember, followingIds);
            }).collect(Collectors.toList());

        // 마지막 항목의 ID 설정
        Long lastId = followMembers.isEmpty() ?
            requestDTO.getLastId() :    // 조회된 목록이 비어있는 경우를 대비해 삼항 연산자 사용
            followMembers.get(followMembers.size() - 1)
                .getId(); // lastId를 요청 DTO의 값이 아닌, 실제 조회된 마지막 항목의 ID로 설정

        String lastNickname = followMembers.isEmpty() ? requestDTO.getLastNickname() :
            followMembers.get(followMembers.size() - 1).getNickname();

        return ScrollResponseDTO.<OtherMemberInfoResponseDTO>withAll()
            .content(dtoList)
            .hasNext(hasNext)
            .lastId(lastId)
            .lastNickname(lastNickname)  // lastNickname 필드도 추가
            .numberOfElements(dtoList.size())
            .totalElements(totalElements)
            .build();
    }


}
