package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.bookmark.BookmarkRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.service.MyBookmarkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/bookmark")
public class MyBookMarkController {

    private final MyBookmarkService myBookmarkService;
    
    @PostMapping
    public ResponseEntity<ApiResponseDTO<Void>> insertBookMark(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @RequestBody @Valid BookmarkRequestDTO requestDTO) {
        
        myBookmarkService.insertBookMark(memberSecurityDTO.getId(), requestDTO);
        
        // 성공적으로 처리되었음을 나타내는 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(null));
    }

}
