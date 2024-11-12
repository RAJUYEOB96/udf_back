package com.undefinedus.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.request.book.BookStatusRequestDTO;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.MemberRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.parser.Authorization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Log4j2
class MyBookServiceImplTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AladinBookRepository aladinBookRepository;

    @Test
    @DisplayName("BookService 연결 확인 테스트")
    void checkConnections() {
        assertNotNull(bookService, "BookService가 주입되지 않았습니다.");
    }

    @Test
    @DisplayName("existsBook 메서드 테스트")
    void testExistsBook() {
        // given
        Long memberId = 1L;

        String isbn13 = "11";
        // when
        boolean result = bookService.existsBook(memberId, isbn13);

        // then
        System.out.println("result = " + result);
    }

    @Test
    @DisplayName("insertNewBookByStatus 메서드 테스트")
    @Commit
    @Rollback(value = false)
    void testInsertNewBookByStatus() {
        // given
        Long memberId = 1L;

        String tabCondition = "stopped"; // completed, wish, reading, stopped

        Long aladinBookId = 1L;

        AladinBook savedAladinBook = aladinBookRepository.findById(aladinBookId)
            .orElseThrow(() -> new IllegalArgumentException("해당 도서를 찾을 수 없습니다. : " + aladinBookId));

        BookStatusRequestDTO requestDTO = BookStatusRequestDTO.builder()
            .myRating(4.5)
            .oneLineReview("test 뭐먹을거야?")
            .currentPage(50)
            .startDate(LocalDate.now().minusDays(3))
            .endDate(LocalDate.now())
            .build();
        // when & then
        Assertions.assertDoesNotThrow(
            () -> bookService.insertNewBookByStatus(memberId, tabCondition, savedAladinBook, requestDTO)
        );

    }

}