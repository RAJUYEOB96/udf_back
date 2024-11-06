package com.undefinedus.backend.service;

import static org.junit.jupiter.api.Assertions.*;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.dto.request.book.AladinBookRequestDTO;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Log4j2
class AladinBookServiceImplTest {

    @Autowired
    private AladinBookService aladinBookService;

    @Test
    @DisplayName("AladinBookService 연결 확인 테스트")
    void checkConnections() {
        assertNotNull(aladinBookService, "AladinBookService가 주입되지 않았습니다.");
    }

    @Test
    @DisplayName("existsAladinBook 메서드 테스트")
    void testExistsAladinBook() {
        // given
        String isbn13 = "1"; // 실제 ISBN13값은 다름

        // when
        Optional<AladinBook> isExists = aladinBookService.existsAladinBook(isbn13);

        // then
        log.info("isExists : " + isExists.get());
    }

    @Test
    @DisplayName("insertAladinBook 메서드 테스트")
    void testInsertAladinBook() {
        // given
        AladinBookRequestDTO requestDTO = AladinBookRequestDTO.builder()
            .isbn13("10")
            .title("test")
            .subTitle("sub")
            .author("test")
            .summary("test")
            .link("https://www.aladin.co.kr/home/welcome.aspx")
            .cover("test.jpg")
            .description("test test")
            .publisher("test")
            .category("국내도서>소설>판타지")
            .customerReviewRank(5.0)
            .isAdult(false)
            .pagesCount(300)
            .build();

        // when
        AladinBook aladinBook = aladinBookService.insertAladinBook(requestDTO);

        // then
        log.info("aladinBook : " + aladinBook);
    }

}