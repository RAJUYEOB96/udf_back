package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.bookmark.BookmarkRequestDTO;
import com.undefinedus.backend.dto.request.bookmark.MyBookmarkUpdateRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.book.MyBookResponseDTO;
import com.undefinedus.backend.dto.response.bookmark.MyBookmarkResponseDTO;
import com.undefinedus.backend.service.MyBookService;
import com.undefinedus.backend.service.MyBookmarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/bookmark")
public class MyBookmarkController {

    private final MyBookmarkService myBookmarkService;
    private final MyBookService myBookService;
    
    @PostMapping
    public ResponseEntity<ApiResponseDTO<Void>> insertBookMark(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @RequestBody @Valid BookmarkRequestDTO requestDTO) {
        
        myBookmarkService.insertBookMark(memberSecurityDTO.getId(), requestDTO);
        
        // 성공적으로 처리되었음을 나타내는 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(null));
    }
    
    // 책 추가에서 들어가 검색할 때 쓰는 컨트롤러 // MyBook에 자기가 기록한 것들만 들고옴
    // 공통으로 쓸건 쓰기 위해 반환을 MyBookResponseDTO 이걸로 함
    @GetMapping("/addSearch")
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<MyBookResponseDTO>>> getSearchMyBook(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @ModelAttribute ScrollRequestDTO requestDTO) {  // requestDTO의 status가 null 또는 빈값이 들어와야함, 그래야 전체 가져옴
        
        Long memberId = memberSecurityDTO.getId();
        
        requestDTO.makeStatusNull(); // 확정적으로 status를 null로 만들어 주기 위해
        
        // Service에서는 ScrollResponseDTO를 반환해야 함
        ScrollResponseDTO<MyBookResponseDTO> response = myBookService.getMyBookList(memberId, requestDTO);
        
        // 여기에서는 자신이 기록한 myBook 다 보여줄 예정
        return ResponseEntity.ok(ApiResponseDTO.success(response)); // GET 요청에서는 body를 사용하지 않는 것이 HTTP 표준
    }
    
    // 내 책장의 책갈피 탭을 했을때 사용하는 컨트롤러
    @GetMapping
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<MyBookmarkResponseDTO>>> getBookmarkList(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @ModelAttribute ScrollRequestDTO requestDTO) {
        
        Long memberId = memberSecurityDTO.getId();
        
        // Service에서는 ScrollResponseDTO를 반환해야 함
        ScrollResponseDTO<MyBookmarkResponseDTO> response = myBookmarkService.getMyBookmarkList(memberId,requestDTO);
    
        return ResponseEntity.ok(ApiResponseDTO.success((response)));
    }
    
    @PatchMapping("/{bookmarkId}")
    public ResponseEntity<ApiResponseDTO<Void>> updateMyBookmark(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @PathVariable("bookmarkId") Long bookmarkId,
            @RequestBody @Valid MyBookmarkUpdateRequestDTO requestDTO) {
        
        myBookmarkService.updateMyBookmark(memberSecurityDTO.getId(), bookmarkId, requestDTO);
        
        return ResponseEntity.ok().body(ApiResponseDTO.success(null));
    }
    
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteMyBookmark(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @PathVariable("bookmarkId") Long bookmarkId) {
        
        myBookmarkService.deleteMyBookmark(memberSecurityDTO.getId(), bookmarkId);
        
        return ResponseEntity.ok().body(ApiResponseDTO.success(null));
    }
}
