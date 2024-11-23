package com.undefinedus.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBookmark;
import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.exception.bookmark.BookmarkNotFoundException;
import com.undefinedus.backend.repository.queryDSL.MyBookmarkRepositoryCustomImpl;
import jakarta.persistence.EntityManager;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
    
    @Nested
    @DisplayName("findByIdAndMemberId 메소드 테스트")
    class FindByIdAndMemberIdTest {
        
        private MyBookmark savedBookmark;
        
        @BeforeEach
        void setUpBookmark() {
            // 테스트용 책 생성
            AladinBook book = AladinBook.builder()
                    .isbn13("978895623") // 일부러 13자 안되게 함
                    .title("테스트 책")
                    .author("테스트 작가")
                    .link("http://example.com/test")
                    .cover("http://example.com/cover/test")
                    .fullDescription("테스트 설명")
                    .publisher("테스트출판사")
                    .categoryName("IT/컴퓨터")
                    .customerReviewRank(4.0)
                    .itemPage(300)
                    .build();
            em.persist(book);
            
            // 테스트용 북마크 생성
            savedBookmark = MyBookmark.builder()
                    .member(member)
                    .aladinBook(book)
                    .phrase("테스트 구절")
                    .pageNumber(100)
                    .build();
            em.persist(savedBookmark);
            
            em.flush();
            em.clear();
        }
        
        @Test
        @DisplayName("존재하는 북마크와 멤버 ID로 조회 성공")
        void findExistingBookmarkSuccess() {
            // when
            MyBookmark foundBookmark = myBookmarkRepository.findByIdAndMemberId(
                    savedBookmark.getId(),
                    member.getId()
            ).orElseThrow(() ->
                    new BookmarkNotFoundException("해당 북마크를 찾을 수 없습니다. : memberId = " + member.getId() + "bookmarkId ="
                            + " " + savedBookmark.getId()));
            
            // then
            assertThat(foundBookmark).isNotNull();
            assertThat(foundBookmark.getId()).isEqualTo(savedBookmark.getId());
            assertThat(foundBookmark.getMember().getId()).isEqualTo(member.getId());
            assertThat(foundBookmark.getPhrase()).isEqualTo("테스트 구절");
            assertThat(foundBookmark.getPageNumber()).isEqualTo(100);
        }
        
        @Test
        @DisplayName("존재하지 않는 북마크 ID로 조회 시 null 반환")
        void findNonExistingBookmarkReturnsNull() {
            // when
            Optional<MyBookmark> result = myBookmarkRepository.findByIdAndMemberId(999999L, member.getId());
            
            // then
            assertThat(result).isEmpty();
        }
        
        @Test
        @DisplayName("존재하지 않는 멤버 ID로 조회 시 빈 Optional 반환")
        void findWithNonExistingMemberReturnsNull() {
            // given
            Long nonExistingMemberId = 999L;
            Long existingBookmarkId = savedBookmark.getId();
            
            // when
            Optional<MyBookmark> result = myBookmarkRepository.findByIdAndMemberId(
                    existingBookmarkId,
                    nonExistingMemberId
            );
            
            // then
            assertThat(result).isEmpty();
        }
        
        @Test
        @DisplayName("다른 멤버의 북마크 조회 시 null 반환")
        void findOtherMemberBookmarkReturnsNull() {
            // given
            Member otherMember = Member.builder()
                    .username("other@test.com")
                    .password("password123")
                    .nickname("other")
                    .memberRoleList(List.of(MemberType.USER))
                    .isPublic(true)
                    .build();
            em.persist(otherMember);
            
            // when
            Optional<MyBookmark> foundBookmark = myBookmarkRepository.findByIdAndMemberId(
                    savedBookmark.getId(),
                    otherMember.getId()
            );
            
            // then
            assertThat(foundBookmark).isEmpty();
        }
    }
    
    @Nested
    @DisplayName("다른 사용자의 북마크를 내 북마크로 복사 테스트")
    class InsertOtherMemberBookmarkTest {
        
        private Member otherMember;
        private MyBookmark otherMemberBookmark;
        
        @BeforeEach
        void setUpOtherMemberBookmark() {
            // 다른 사용자 생성
            otherMember = Member.builder()
                    .username("other@test.com")
                    .password("password123")
                    .nickname("other")
                    .memberRoleList(List.of(MemberType.USER))
                    .isPublic(true)
                    .build();
            em.persist(otherMember);
            
            // 다른 사용자의 북마크용 책 생성
            AladinBook book = AladinBook.builder()
                    .isbn13("9788956740123")
                    .title("다른 사용자의 책")
                    .author("테스트 작가")
                    .link("http://example.com/other")
                    .cover("http://example.com/cover/other")
                    .fullDescription("다른 사용자의 책 설명")
                    .publisher("테스트출판사")
                    .categoryName("IT/컴퓨터")
                    .customerReviewRank(4.5)
                    .itemPage(400)
                    .build();
            em.persist(book);
            
            // 다른 사용자의 북마크 생성
            otherMemberBookmark = MyBookmark.builder()
                    .member(otherMember)
                    .aladinBook(book)
                    .phrase("다른 사용자의 인상적인 구절")
                    .pageNumber(150)
                    .build();
            em.persist(otherMemberBookmark);
            
            em.flush();
            em.clear();
        }
        
        @Test
        @DisplayName("다른 사용자의 북마크를 내 북마크로 성공적으로 복사")
        void insertOtherMemberBookmarkSuccess() {
            // when
            MyBookmark myBookmark = MyBookmark.builder()
                    .member(member)
                    .aladinBook(otherMemberBookmark.getAladinBook())
                    .phrase(otherMemberBookmark.getPhrase())
                    .pageNumber(otherMemberBookmark.getPageNumber())
                    .build();
            MyBookmark savedBookmark = myBookmarkRepository.save(myBookmark);
            
            // then
            assertThat(savedBookmark).isNotNull();
            assertThat(savedBookmark.getMember().getId()).isEqualTo(member.getId());
            assertThat(savedBookmark.getAladinBook().getIsbn13()).isEqualTo(otherMemberBookmark.getAladinBook().getIsbn13());
            assertThat(savedBookmark.getPhrase()).isEqualTo(otherMemberBookmark.getPhrase());
            assertThat(savedBookmark.getPageNumber()).isEqualTo(otherMemberBookmark.getPageNumber());
        }
        
        @Test
        @DisplayName("복사된 북마크는 원본과 다른 ID를 가짐")
        void insertedBookmarkHasDifferentId() {
            // when
            MyBookmark myBookmark = MyBookmark.builder()
                    .member(member)
                    .aladinBook(otherMemberBookmark.getAladinBook())
                    .phrase(otherMemberBookmark.getPhrase())
                    .pageNumber(otherMemberBookmark.getPageNumber())
                    .build();
            MyBookmark savedBookmark = myBookmarkRepository.save(myBookmark);
            
            // then
            assertThat(savedBookmark.getId()).isNotNull();
            assertThat(savedBookmark.getId()).isNotEqualTo(otherMemberBookmark.getId());
        }
        
        @Test
        @DisplayName("복사된 북마크의 책 정보는 원본과 동일한 참조를 가짐")
        void insertedBookmarkSharesSameBookReference() {
            // when
            MyBookmark myBookmark = MyBookmark.builder()
                    .member(member)
                    .aladinBook(otherMemberBookmark.getAladinBook())
                    .phrase(otherMemberBookmark.getPhrase())
                    .pageNumber(otherMemberBookmark.getPageNumber())
                    .build();
            MyBookmark savedBookmark = myBookmarkRepository.save(myBookmark);
            
            // then
            assertThat(savedBookmark.getAladinBook().getId()).isEqualTo(otherMemberBookmark.getAladinBook().getId());
        }
    }
}