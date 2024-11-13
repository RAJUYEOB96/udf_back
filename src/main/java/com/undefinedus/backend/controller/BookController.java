package com.undefinedus.backend.controller;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.dto.request.book.BookRequestDTO;
import com.undefinedus.backend.service.AladinBookService;
import com.undefinedus.backend.service.BookService;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/book")
public class BookController {

    private final BookService bookService;
    private final AladinBookService aladinBookService;

    @PostMapping("/{memberId}")
    public Map<String, String>  insertBook(
        @PathVariable("memberId") Long memberId,
        @RequestBody BookRequestDTO requestDTO
        ) {

        Optional<AladinBook> isExistsAladinBook = aladinBookService.existsAladinBook(
            requestDTO.getAladinBookRequestDTO().getIsbn13());

        AladinBook savedAladinBook = null;

        if (isExistsAladinBook.isEmpty()) {
            savedAladinBook = aladinBookService.insertAladinBook(
                requestDTO.getAladinBookRequestDTO());
        } else {
            savedAladinBook = isExistsAladinBook.get();
        }

        boolean isExistsBook = bookService.existsBook(memberId,
            requestDTO.getAladinBookRequestDTO().getIsbn13());

        if (isExistsBook) {
            return Map.of("result", "error", "msg", "이미 내 책장에 기록된 도서입니다.");
        }

        bookService.insertNewBookByStatus(memberId, requestDTO.getTapCondition(), savedAladinBook, requestDTO.getBookStatusRequestDTO());

        return Map.of("result", "success");
    }



}
