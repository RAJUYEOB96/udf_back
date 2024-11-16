package com.undefinedus.backend.service;

// 필요한 static import들 - 테스트 및 Mockito 관련 메서드들을 더 깔끔하게 사용하기 위함
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBookmark;
import com.undefinedus.backend.dto.request.bookmark.BookmarkRequestDTO;
import com.undefinedus.backend.exception.book.BookNotFoundException;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookmarkRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)  // JUnit5에서 Mockito를 사용하기 위한 설정
class MyBookmarkServiceImplTest {
    
    // @Mock: 가짜 객체를 만들어주는 어노테이션
    // 실제 데이터베이스 대신 가짜 객체를 사용하여 테스트
    // 테스트에 필요한 가짜(Mock) 객체 생성
    @Mock
    private MyBookmarkRepository myBookmarkRepository;
    
    @Mock
    private AladinBookRepository aladinBookRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @InjectMocks // Mock 객체들을 주입받는 객체   // 위에서 생성한 Mock 객체들이 자동으로 주입됨
    private MyBookmarkServiceImpl myBookmarkService;
    
    // 테스트에서 반복적으로 사용할 객체들을 필드로 선언
    private Member mockMember;
    private AladinBook mockAladinBook;
    private BookmarkRequestDTO mockRequestDTO;
    
    @BeforeEach
    void setUp() {
        // 테스트용 Member 객체 생성
        mockMember = Member.builder()
                .id(1L)
                .username("test@test.com")
                .nickname("테스트")
                .build();
        
        // 테스트용 AladinBook 객체 생성
        mockAladinBook = AladinBook.builder()
                .title("테스트 책")
                .author("테스트 작가")
                .isbn13("1234567890")
                .build();
        
        // 테스트용 BookmarkRequestDTO 객체 생성
        mockRequestDTO = BookmarkRequestDTO.builder()
                .title("테스트 책")
                .phrase("테스트 문구")
                .bookmarkPage(100)
                .build();
    }
    
    @Test
    @DisplayName("북마크 생성 성공 테스트")
    void insertBookMark_Success() {
        // given
        // memberRepository.findById() 호출 시 mockMember를 반환하도록 설정
        // when과 given은 결과적으로는 동일한 동작을 하지만,
        // 테스트 코드의 가독성과 일관성을 위해 보통은 한 가지 방식을 선택해서 사용합니다.
        // BDD(Behavior-Driven Development) 스타일의 테스트를 작성할 때는 given()을 더 선호하는 편입니다.
        given(memberRepository.findById(1L))
                .willReturn(Optional.of(mockMember));
        
        given(aladinBookRepository.findByTitle("테스트 책"))
                .willReturn(Optional.of(mockAladinBook));
        
        // when
        myBookmarkService.insertBookMark(1L, mockRequestDTO);
        
        // then
        verify(myBookmarkRepository).save(any(MyBookmark.class));
    }
    
    @Test
    @DisplayName("존재하지 않는 회원으로 북마크 생성 시 실패")
    void insertBookMark_MemberNotFound() {
        // given
        // 책은 존재하도록 설정
        given(aladinBookRepository.findByTitle("테스트 책"))
                .willReturn(Optional.of(mockAladinBook));
        
        given(memberRepository.findById(1L))
                .willReturn(Optional.empty());
        
        // when & then
        
        assertThatThrownBy(() -> myBookmarkService.insertBookMark(1L, mockRequestDTO))
                .isInstanceOf(MemberNotFoundException.class)
                .hasMessageContaining("해당 유저를 찾을 수 없습니다.");
    }
    
    @Test
    @DisplayName("존재하지 않는 책으로 북마크 생성 시 실패")
    void insertBookMark_BookNotFound() {
        // given
        given(aladinBookRepository.findByTitle("테스트 책"))
                .willReturn(Optional.empty());
        
        // when & then
        assertThatThrownBy(() -> myBookmarkService.insertBookMark(1L, mockRequestDTO))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("해당 책을 찾을 수 없습니다.");
    }
}