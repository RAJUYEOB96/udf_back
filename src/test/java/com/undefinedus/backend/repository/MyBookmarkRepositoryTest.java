package com.undefinedus.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBookmark;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.repository.queryDSL.MyBookmarkRepositoryCustomImpl;
import jakarta.persistence.EntityManager;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MyBookmarkRepositoryTest {
    
    @Autowired
    private MyBookmarkRepository myBookmarkRepository;
    
    @Autowired
    private EntityManager em;
    
    private Member member;
    
    @BeforeEach
    void setUp() {
        // === Member 생성 === //
        member = Member.builder()
                .username("test@test.com")
                .password("password123")
                .nickname("tester")
                .memberRoleList(List.of(MemberType.USER))
                .isPublic(true)
                .build();
        em.persist(member);
        
        // === 10개의 테스트용 북마크 데이터 생성 === //
        for (int i = 1; i <= 10; i++) {
            AladinBook book = AladinBook.builder()
                    .isbn13(String.format("978895674%04d", i))
                    .title("테스트 책 " + i)
                    .author(i % 2 == 0 ? "김작가" : "이작가")
                    .link("http://example.com/" + i)
                    .cover("http://example.com/cover" + i)
                    .fullDescription("테스트 설명 " + i)
                    .publisher("테스트출판사")
                    .categoryName("IT/컴퓨터")
                    .customerReviewRank(3.8)
                    .itemPage(500)
                    .build();
            em.persist(book);
            
            MyBookmark bookmark = MyBookmark.builder()
                    .member(member)
                    .aladinBook(book)
                    .phrase("인상 깊은 구절 " + i)
                    .pageNumber(i * 10)
                    .build();
            em.persist(bookmark);
        }
        
        em.flush();
        em.clear();
    }
    
    @Nested
    @DisplayName("북마크 목록 무한 스크롤 조회 테스트")
    class FindBookmarksWithScrollTest {
        
        @Test
        @DisplayName("기본 조회 - 첫 페이지, 내림차순")
        void findFirstPageDescTest() {
            // given
            ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                    .size(3)
                    .sort("desc")
                    .build();
            
            // when
            List<MyBookmark> result = myBookmarkRepository.findBookmarksWithScroll(member.getId(), requestDTO);
            
            // then
            assertThat(result).hasSize(4); // size + 1개 반환
            
            assertThat(result)
                    .extracting(bookmark -> bookmark.getCreatedDate())
                    .isSortedAccordingTo(Comparator.reverseOrder());
        }
        
        @Test
        @DisplayName("무한 스크롤 - 두 번째 페이지 조회")
        void findSecondPageTest() {
            // given
            // 1. 첫 페이지 조회
            ScrollRequestDTO firstRequest = ScrollRequestDTO.builder()
                    .size(3)
                    .sort("desc")
                    .build();
            
            List<MyBookmark> firstPage = myBookmarkRepository.findBookmarksWithScroll(member.getId(), firstRequest);
            Long lastId = firstPage.get(2).getId(); // 세 번째 항목의 ID
            
            // 2. 두 번째 페이지 조회
            ScrollRequestDTO secondRequest = ScrollRequestDTO.builder()
                    .lastId(lastId)
                    .size(3)
                    .sort("desc")
                    .build();
            
            // when
            List<MyBookmark> secondPage = myBookmarkRepository.findBookmarksWithScroll(member.getId(), secondRequest);
            
            // then
            assertThat(secondPage).hasSize(4);
            assertThat(secondPage)
                    .extracting("id")
                    .allMatch(id -> (Long) id < lastId);
        }
        
        @Test
        @DisplayName("검색어로 책 제목 검색")
        void findByBookTitleTest() {
            // given
            ScrollRequestDTO request = ScrollRequestDTO.builder()
                    .size(10)
                    .search("테스트 책 1")
                    .build();
            
            // when
            List<MyBookmark> result = myBookmarkRepository.findBookmarksWithScroll(member.getId(), request);
            
            // then
            assertThat(result)
                    .isNotEmpty()
                    .extracting("aladinBook.title")
                    .allMatch(title -> title.toString().contains("테스트 책 1"));
        }
        
        @Test
        @DisplayName("검색어로 북마크 구절 검색")
        void findByPhraseTest() {
            // given
            ScrollRequestDTO request = ScrollRequestDTO.builder()
                    .size(10)
                    .search("인상 깊은 구절 1")
                    .build();
            
            // when
            List<MyBookmark> result = myBookmarkRepository.findBookmarksWithScroll(member.getId(), request);
            
            // then
            assertThat(result)
                    .isNotEmpty()
                    .extracting("phrase")
                    .allMatch(phrase -> phrase.toString().contains("인상 깊은 구절 1"));
        }
        
        @Test
        @DisplayName("데이터가 없는 경우")
        void findWithNoDataTest() {
            // given
            ScrollRequestDTO request = ScrollRequestDTO.builder()
                    .size(3)
                    .sort("desc")
                    .search("존재하지않는데이터")
                    .build();
            
            // when
            List<MyBookmark> result = myBookmarkRepository.findBookmarksWithScroll(member.getId(), request);
            
            // then
            assertThat(result).isEmpty();
        }
    }
}