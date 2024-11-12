package com.undefinedus.backend.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.undefinedus.backend.domain.entity.AladinBook;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@Log4j2
class AladinMyMyBookRepositoryTest {

    @Autowired
    private AladinBookRepository aladinBookRepository;

    @Test
    @DisplayName("AladinBookRepository 연결 확인 테스트")
    void checkConnections() {
        assertNotNull(aladinBookRepository, "AladinBookRepository가 주입되지 않았습니다.");
    }

    @Test
    @DisplayName("테스트용 AladinBook 10개 생성")
    @Commit
    void create10TestAladinBook(){

        for (int i = 0; i < 10; i++) {
            AladinBook aladinBook = AladinBook.builder()
                .isbn13(String.valueOf(i))
                .title("test" + i)
                .subTitle("sub" + i)
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

            aladinBookRepository.save(aladinBook);
        }

    }

    @Test
    @DisplayName("findByIsbn13 메서드 테스트")
    void testFindByIsbn13() {

        // given
        AladinBook getAladinBook = null;
        String isbn13 = "1"; // 실제 ISBN13의 값은 다름

        // when
        Optional<AladinBook> findAladinBook = aladinBookRepository.findByIsbn13(isbn13);

        // then
        if (findAladinBook.isPresent()) {
            getAladinBook = findAladinBook.get();
        }

        log.info("getAladinBook : " + getAladinBook);
    }
}