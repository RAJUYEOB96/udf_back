package com.undefinedus.backend.controller;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.request.BookScrollRequestDTO;
import com.undefinedus.backend.dto.request.book.BookRequestDTO;
import com.undefinedus.backend.dto.request.book.BookStatusRequestDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.book.MyBookResponseDTO;
import com.undefinedus.backend.exception.book.BookExistsException;
import com.undefinedus.backend.exception.book.BookNotFoundException;
import com.undefinedus.backend.service.AladinBookService;
import com.undefinedus.backend.service.MyBookService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/books")
public class MyBookController {
    
    private final MyBookService myBookService;
    private final AladinBookService aladinBookService;
    
    @PostMapping
    public ResponseEntity<ApiResponseDTO<Void>> insertBook(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @RequestBody @Valid BookRequestDTO requestDTO) {  // @Valid 추가
        
        Long memberId = memberSecurityDTO.getId();
        
        try {
            // MyBook 테이블에 이미 있는지 확인
            if (!myBookService.existsBook(memberId, requestDTO.getAladinBookRequestDTO().getIsbn13())) {
                // AladinBook 가져오거나 없으면 새로 생성
                Optional<AladinBook> isExistsAladinBook = aladinBookService.existsAladinBook(
                        requestDTO.getAladinBookRequestDTO().getIsbn13());
                
                AladinBook savedAladinBook = isExistsAladinBook.orElseGet(() ->
                        aladinBookService.insertAladinBook(requestDTO.getAladinBookRequestDTO()));
                
                // MyBook에 새로 추가
                myBookService.insertNewBookByStatus(memberId, savedAladinBook, requestDTO.getBookStatusRequestDTO());
            }
        } catch (Exception e) {
            log.error("책 처리 중 오류가 발생했습니다.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("책 처리에 실패했습니다."));
        }
        
        // 성공적으로 처리되었음을 나타내는 응답을 반환합니다.
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponseDTO.success(null));
    }
    
    // GET 요청은 URL의 query parameter로 데이터 전달
    // 각 파라미터가 key=value 형태로 전달됨
    @GetMapping
    public ResponseEntity<ApiResponseDTO<ScrollResponseDTO<MyBookResponseDTO>>> getMyBookList(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
            @ModelAttribute BookScrollRequestDTO requestDTO) {  // GET 요청에서는 @RequestBody 대신 @ModelAttribute 사용
        
        Long memberId = memberSecurityDTO.getId();

        // Service에서는 ScrollResponseDTO를 반환해야 함
        ScrollResponseDTO<MyBookResponseDTO> response = myBookService.getMyBookList(memberId, requestDTO);
        
        return ResponseEntity.ok(ApiResponseDTO.success(response)); // GET 요청에서는 body를 사용하지 않는 것이 HTTP 표준
    }
    
    @PatchMapping("/{bookId}")
    public ResponseEntity<ApiResponseDTO<Void>> updateBookStatus(
            @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO, // 인증된 사용자 정보를 주입받습니다.
            @PathVariable("bookId") Long bookId,
            @RequestBody @Valid BookStatusRequestDTO requestDTO) {
        
        try {
            // 해당 사용자의 책장에서 지정된 책의 상태를 업데이트합니다.
            myBookService.updateBookStatus(memberSecurityDTO.getId(), bookId, requestDTO);
        } catch (BookNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error(e.getMessage()));
        } catch (Exception e) {
            log.error("도서 상태를 업데이트하지 못했습니다.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDTO.error("책 상태 업데이트에 실패했습니다."));
        }
        
        
        // 성공적으로 처리되었음을 나타내는 응답을 반환합니다.
        return ResponseEntity.ok()
                .body(ApiResponseDTO.success(null));
    }
    
}
