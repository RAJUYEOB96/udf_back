package com.undefinedus.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.exception.book.BookNotFoundException;
import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AladinBookRepositoryTest {

    @Autowired
    private AladinBookRepository aladinBookRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        // === AladinBook 생성 === //
        for (int i = 1; i <= 10; i++) {
            AladinBook aladinBook = AladinBook.builder()
                .isbn13("0000" + i)
                .title("test" + i)
                .author("test" + i)
                .link("test" + i)
                .cover("test" + i)
                .fullDescription("test" + i)
                .fullDescription2("test" + i)
                .publisher("test" + i)
                .categoryName("IT/컴퓨터")
                .customerReviewRank(0.1)
                .build();
            em.persist(aladinBook);
        }
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("findByTitle 메서드 테스트")
    void testFindByTitle() {
        // given
        String title = "test1";

        // when
        AladinBook foundBook = aladinBookRepository.findByTitle(title)
            .orElseThrow(() -> new BookNotFoundException("해당 책을 찾을 수 없습니다. : " + title));

        // then
        assertThat(foundBook).isNotNull();
        assertThat(foundBook.getTitle()).isEqualTo(title);
        assertThat(foundBook.getIsbn13()).isEqualTo("00001");
        assertThat(foundBook.getAuthor()).isEqualTo("test1");
        assertThat(foundBook.getCategoryName()).isEqualTo("IT/컴퓨터");
    }

    @Test
    @DisplayName("findByTitle 메서드 테스트 - 책을 찾지 못한 경우")
    void testFindByTitleNotFound() {
        // given
        String title = "존재하지 않는 책";

        // when & then
        assertThatThrownBy(() -> aladinBookRepository.findByTitle(title)
            .orElseThrow(() -> new BookNotFoundException("해당 책을 찾을 수 없습니다. : " + title)))
            .isInstanceOf(BookNotFoundException.class)
            .hasMessageContaining("해당 책을 찾을 수 없습니다.");
    }
}