package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.book.BookStatusRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.book.MyBookResponseDTO;
import com.undefinedus.backend.dto.response.bookmark.MyBookmarkResponseDTO;
import com.undefinedus.backend.dto.response.social.MemberSocialInfoResponseDTO;
import com.undefinedus.backend.dto.response.social.OtherMemberInfoResponseDTO;
import com.undefinedus.backend.service.MyBookService;
import com.undefinedus.backend.service.MyBookmarkService;
import com.undefinedus.backend.service.SocialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/social")
public class SocialController {
    private final SocialService socialService;
    private final MyBookService myBookService;
    private final MyBookmarkService myBookmarkService;
    
    // 소셜 메인 (MAIN_0004), 팔로잉 팔로워(SOCIAL_0001) 목록에서 나의 정보 가져가기
    @GetMapping("/myInfo")
    public ResponseEntity<ApiResponseDTO<MemberSocialInfoResponseDTO>> getMySimpleSocialInfo(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO) {
        
        Long memberId = memberSecurityDTO.getId();
        
        MemberSocialInfoResponseDTO response = socialService.getMemberSocialSimpleInfo(memberId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }
    
    // 소셜 메인 (MAIN_0004)에서 닉네임으로 검색했을 때 가져오는 리스트 (자신 제외, 모든 멤버 리스트)
    @GetMapping("/main/search")
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<OtherMemberInfoResponseDTO>>> getOtherMemberList(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @ModelAttribute ScrollRequestDTO requestDTO) {
        
        requestDTO.updateSize(100); // 소셜을 불러올때는 100을 기본으로
        
        Long memberId = memberSecurityDTO.getId();
        
        ScrollResponseDTO<OtherMemberInfoResponseDTO> response = socialService.getOtherMembers(memberId, requestDTO);
        
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }
    
    // 팔로우 (SOCIAL_0001) 에서 닉네임으로 검색했을 때 가져오는 리스트 (각각)
    @GetMapping("/follow/search")
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<OtherMemberInfoResponseDTO>>> getMemberFollowList(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @ModelAttribute ScrollRequestDTO requestDTO) {  // tabCondition에 의해 팔로워 팔로잉 리스트 구분되서 가져옴 (없으면 에러)
                                                            // search 가 비어있으면 전체 검색 (tabCondition에 따른)
                                                            // 보낼때 lastId(필요 없지만), lastNickname 둘다 보내기
        Long memberId = memberSecurityDTO.getId();
        
        ScrollResponseDTO<OtherMemberInfoResponseDTO> response = socialService.getMemberFollows(memberId, requestDTO);
        
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }
    
    @PatchMapping("/follow/{targetMemberId}") // 내가 팔로우 상태를 변경시킬 상대의 memberId
    public ResponseEntity<ApiResponseDTO<Void>> toggleFollowStatus(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @PathVariable("targetMemberId") Long targetMemberId) {
        
        Long myMemberId = memberSecurityDTO.getId();
        
        socialService.toggleFollowStatus(myMemberId, targetMemberId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(null));
    }
    
    // 소셜 메인 (SOCIAL_0003), 팔로잉 팔로워(SOCIAL_0001) 목록에서 타겟 멤버의 정보 가져가기
    @GetMapping("/otherInfo/{targetMemberId}")
    public ResponseEntity<ApiResponseDTO<MemberSocialInfoResponseDTO>> getOtherMemberInfo(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @PathVariable("targetMemberId") Long targetMemberId) {
        
        Long myMemberId = memberSecurityDTO.getId();
        
        MemberSocialInfoResponseDTO response = socialService.getOtherMemberSocialSimpleInfo(myMemberId, targetMemberId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }
    
    // 팔로우 (SOCIAL_0001) 에서 닉네임으로 검색했을 때 가져오는 Follow 멤버 리스트 (각각)
    @GetMapping("/follow/search/{targetMemberId}")
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<OtherMemberInfoResponseDTO>>> getOtherMemberFollowList(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @PathVariable("targetMemberId") Long targetMemberId,
            @ModelAttribute ScrollRequestDTO requestDTO) {  // tabCondition에 의해 팔로워 팔로잉 리스트 구분되서 가져옴 (없으면 에러)
        // search 가 비어있으면 전체 검색 (tabCondition에 따른)
        // 보낼때 lastId(필요 없지만), lastNickname 둘다 보내기
        Long memberId = memberSecurityDTO.getId();
        
        requestDTO.updateSize(100); // 팔로우 리스트는 기본 100개 씩 검색
        
        ScrollResponseDTO<OtherMemberInfoResponseDTO> response = socialService.getOtherMemberFollows(memberId,
                targetMemberId, requestDTO);
        
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }
    
    // 소셜 책장 메인(SOCIAL_0003) 에서 타겟멤버의 책장 리스트 가져오기
    // MyBookController getMyBookList 메서드 참고
    @GetMapping("/other/books/{targetMemberId}")
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<MyBookResponseDTO>>> getOtherMemberBookList(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @PathVariable("targetMemberId") Long targetMemberId,
            @ModelAttribute ScrollRequestDTO requestDTO) {
        
        Long loginMemberId = memberSecurityDTO.getId();
        
        ScrollResponseDTO<MyBookResponseDTO> response = myBookService.getOtherMemberBookList(loginMemberId,
                targetMemberId, requestDTO);
        
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }
    
    // 소셜 책 상세 (SOCIAL_0005,7,9,11) 에서 타겟 멤버 책 상세 가져오기
    // MyBookController getMyBookDetail 메서드 참고
    @GetMapping("/other/books/{targetMemberId}/{myBookId}")
    public ResponseEntity<ApiResponseDTO<MyBookResponseDTO>> getOtherMemberBookDetail(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @PathVariable("targetMemberId") Long targetMemberId,
            @PathVariable("myBookId") Long myBookId) {
        
        Long loginMemberId = memberSecurityDTO.getId();
        
        MyBookResponseDTO findBook = myBookService.getOtherMemberBook(loginMemberId, targetMemberId, myBookId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(findBook));
    }
    
    // 소셜 타겟의 책 상세보기에서 status가 null인 경우 + 책담기 부분 (BookStatus.WISH로 고정)
    @PostMapping("/other/books/insert/{targetMyBookId}")
    public ResponseEntity<ApiResponseDTO<Void>> insertNewBookByWish(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @PathVariable("targetMyBookId") Long targetMyBookId) {
        
        Long loginMemberId = memberSecurityDTO.getId();
        
        // 타겟 멤버의 기록된 책중 내가 기록 안한 책을 WISH 타입으로 저장하는 기능
        myBookService.insertNewBookByWish(loginMemberId, targetMyBookId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(null));
    }
    
    // 소셜 책갈피 목록(SOCIAL_0013) 에서 타겟멤버의 책갈피 리스트 가져오는 부분
    // 내 책장의 책갈피 탭을 했을때 사용하는 컨트롤러
    // 모달이기도 하고 상세내용이 적어서 상세는 따로 안만들고 MyBookmarkResponseDTO에 다 담음
    @GetMapping("/other/bookmarks/{targetMemberId}")
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<MyBookmarkResponseDTO>>> getOtherMemberBookmarkList(
            @PathVariable("targetMemberId") Long targetMemberId,
            @ModelAttribute ScrollRequestDTO requestDTO) {
        
        // 이전에 만들어 두었던것 loginMemberId -> targetMemberId 만 변경하고 그대로 사용
        ScrollResponseDTO<MyBookmarkResponseDTO> response =
                myBookmarkService.getMyBookmarkList(targetMemberId, requestDTO);
        
        return ResponseEntity.ok(ApiResponseDTO.success(response));
    }
    
    // 소셜 책갈피 상세(SOCIAL_0014)의 책갈피 담기 기능
    @PostMapping("/other/bookmarks/{targetBookmarkId}")
    public ResponseEntity<ApiResponseDTO<Void>> insertOtherMemberBookmarkToMe(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @PathVariable("targetBookmarkId") Long targetBookmarkId) {
        
        Long LoginMemberId = memberSecurityDTO.getId();
        
        myBookmarkService.insertOtherMemberBookmarkToMe(LoginMemberId, targetBookmarkId);
        
        return ResponseEntity.ok(ApiResponseDTO.success(null));
        
    }
}
