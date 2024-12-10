package com.undefinedus.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.CalendarStamp;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.book.BookStatusRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.book.MyBookResponseDTO;
import com.undefinedus.backend.exception.aladinBook.AladinBookNotFoundException;
import com.undefinedus.backend.exception.book.BookDuplicateNotAllowException;
import com.undefinedus.backend.exception.book.BookNotFoundException;
import com.undefinedus.backend.exception.book.InvalidStatusException;
import com.undefinedus.backend.exception.member.MemberException;
import com.undefinedus.backend.repository.CalendarStampRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)  // JUnit5에서 Mockito를 사용하기 위한 설정
class MyBookServiceImplTest {
    
    // 테스트에 필요한 가짜(Mock) 객체 생성
    @Mock
    private MyBookRepository myBookRepository;
    
    @Mock
    private MemberRepository memberRepository;
    
    @Mock
    private CalendarStampRepository calendarStampRepository;
    
    // Mock 객체들을 주입받는 실제 테스트 대상 서비스
    @InjectMocks
    private MyBookServiceImpl myBookService;
    
    // 테스트에서 공통으로 사용할 테스트 데이터
    private Member testMember;
    private AladinBook testAladinBook;
    private MyBook testMyBook;
    private BookStatusRequestDTO testRequestDTO;
    
    // 각 테스트 실행 전에 실행되는 메서드
    // 테스트 데이터를 초기화
    @BeforeEach
    void setUp() {
        // 테스트용 Member 객체 생성
        testMember = Member.builder()
                .id(1L)
                .username("test@test.com")
                .build();
        
        // 테스트용 AladinBook 객체 생성
        testAladinBook = AladinBook.builder()
                .id(1L)
                .isbn13("9788956746425")
                .title("테스트 책")           // title 추가
                .author("테스트 작가")        // author 추가
                .itemPage(300)              // pagesCount -> itemPage
                .cover("test-cover-url")
                .link("test-link-url")      // link 추가
                .fullDescription("테스트 설명") // fullDescription 추가
                .publisher("테스트 출판사")    // publisher 추가
                .categoryName("IT/컴퓨터")   // categoryName 추가
                .customerReviewRank(4.5)    // customerReviewRank 추가
                .build();
        
        // 테스트용 MyBook 객체 생성
        testMyBook = MyBook.builder()
                .id(1L)
                .member(testMember)
                .aladinBook(testAladinBook)
                .isbn13("9788956746425")
                .status(BookStatus.WISH)
                .build();
        
        // 테스트용 DTO 객체 생성
        testRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.READING.name())
                .currentPage(50)
                .myRating(4.5)
                .build();
        
    }
    
    @Test
    @DisplayName("책 존재 여부 확인 - 존재하는 경우")
    void existsBook_WhenBookExists() {
        // given: 테스트 조건 설정
        // memberId로 Member를 찾으면 testMember를 반환하도록 설정
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        // memberId와 isbn13으로 MyBook을 찾으면 testMyBook을 반환하도록 설정
        when(myBookRepository.findByMemberIdAndIsbn13(anyLong(), anyString()))
                .thenReturn(Optional.of(testMyBook));
        
        // when: 테스트할 메서드 실행
        boolean result = myBookService.existsBook(1L, "9788956746425");
        
        // then: 결과 검증
        // 결과가 true인지 확인
        assertThat(result).isTrue();
        // memberRepository.findById가 1L 파라미터로 호출되었는지 검증
        verify(memberRepository).findById(1L);
        // myBookRepository.findByMemberIdAndIsbn13이 정확한 파라미터로 호출되었는지 검증
        verify(myBookRepository).findByMemberIdAndIsbn13(1L, "9788956746425");
    }
    
    @Test
    @DisplayName("책 존재 여부 확인 - 존재하지 않는 경우")
    void existsBook_WhenBookDoesNotExist() {
        // given: 테스트 조건 설정
        // memberId로 Member는 찾을 수 있지만
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        // MyBook은 찾을 수 없는 상황 설정
        when(myBookRepository.findByMemberIdAndIsbn13(anyLong(), anyString()))
                .thenReturn(Optional.empty());
        
        // when: 테스트할 메서드 실행
        boolean result = myBookService.existsBook(1L, "9788956746425");
        
        // then: 결과 검증
        // 결과가 false인지 확인
        assertThat(result).isFalse();
        // 각 repository 메서드가 정확한 파라미터로 호출되었는지 검증
        verify(memberRepository).findById(1L);
        verify(myBookRepository).findByMemberIdAndIsbn13(1L, "9788956746425");
    }
    
    @Test
    @DisplayName("새 책 등록 - COMPLETED 상태로 등록")
    void insertNewBookByStatus_WhenStatusIsCompleted() {
        // given: 테스트 조건 설정
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        
        // 이 테스트에서만 필요한 save 설정 추가
        when(myBookRepository.save(any(MyBook.class))).thenAnswer(invocation -> {
            MyBook myBook = invocation.getArgument(0);
            myBook.changeId(1L);
            return myBook;
        });
        
        BookStatusRequestDTO completedRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.COMPLETED.name())
                .myRating(4.5)
                .oneLineReview("좋은 책이었습니다.")
                .currentPage(testAladinBook.getItemPage())  // pagesCount -> itemPage
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now())
                .build();
        
        // when: 테스트할 메서드 실행
        myBookService.insertNewBookByStatus(1L, testAladinBook, completedRequestDTO);
        
        // then: 결과 검증
        verify(memberRepository).findById(1L);
        verify(myBookRepository).save(argThat(myBook -> {
            return myBook.getStatus() == BookStatus.COMPLETED &&
                    myBook.getMyRating() == 4.5 &&
                    myBook.getOneLineReview().equals("좋은 책이었습니다.") &&
                    myBook.getCurrentPage() == testAladinBook.getItemPage() && // pagesCount -> itemPage
                    myBook.getStartDate() != null &&
                    myBook.getEndDate() != null;
        }));
        verify(calendarStampRepository).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("새 책 등록 - READING 상태로 등록")
    void insertNewBookByStatus_WhenStatusIsReading() {
        // given: 테스트 조건 설정
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        
        // 이 테스트에서만 필요한 save 설정 추가
        when(myBookRepository.save(any(MyBook.class))).thenAnswer(invocation -> {
            MyBook myBook = invocation.getArgument(0);
            myBook.changeId(1L);
            return myBook;
        });
        
        BookStatusRequestDTO readingRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.READING.name())
                .myRating(0.0)
                .currentPage(50)
                .startDate(LocalDate.now())
                .build();
        
        // when: 테스트할 메서드 실행
        myBookService.insertNewBookByStatus(1L, testAladinBook, readingRequestDTO);
        
        // then: 결과 검증
        verify(memberRepository).findById(1L);
        verify(myBookRepository).save(argThat(myBook -> {
            return myBook.getStatus() == BookStatus.READING &&
                    myBook.getCurrentPage() == 50 &&
                    myBook.getStartDate() != null &&
                    myBook.getEndDate() == null;
        }));
        verify(calendarStampRepository).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("새 책 등록 - WISH 상태로 등록")
    void insertNewBookByStatus_WhenStatusIsWish() {
        // given: 테스트 조건 설정
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        
        BookStatusRequestDTO wishRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.WISH.name())
                .build();
        
        // Mock save 동작 정의 추가
        when(myBookRepository.save(any(MyBook.class))).thenAnswer(invocation -> {
            MyBook book = invocation.getArgument(0);
            // ID 설정 (실제 DB에서처럼 ID가 생성되는 것을 시뮬레이션)
            ReflectionTestUtils.setField(book, "id", 1L);
            return book;
        });
        
        // when: 테스트할 메서드 실행
        myBookService.insertNewBookByStatus(1L, testAladinBook, wishRequestDTO);
        
        // then: 결과 검증
        verify(memberRepository).findById(1L);
        verify(myBookRepository).save(argThat(myBook ->
                myBook.getStatus() == BookStatus.WISH &&
                        myBook.getCurrentPage() == null &&
                        myBook.getMyRating() == null &&
                        myBook.getOneLineReview() == null
        ));
        // WISH 상태에서는 CalendarStamp가 저장되지 않아야 함
        verify(calendarStampRepository, never()).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("새 책 등록 - STOPPED 상태로 등록")
    void insertNewBookByStatus_WhenStatusIsStopped() {
        // given: 테스트 조건 설정
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        
        BookStatusRequestDTO stoppedRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.STOPPED.name())
                .myRating(3.0)
                .oneLineReview("잠시 중단")
                .currentPage(30)
                .startDate(LocalDate.now().minusDays(5))
                .endDate(LocalDate.now())
                .build();
        
        // Mock save 동작 정의 추가
        when(myBookRepository.save(any(MyBook.class))).thenAnswer(invocation -> {
            MyBook book = invocation.getArgument(0);
            // ID 설정 (실제 DB에서처럼 ID가 생성되는 것을 시뮬레이션)
            ReflectionTestUtils.setField(book, "id", 1L);
            return book;
        });
        
        // when: 테스트할 메서드 실행
        myBookService.insertNewBookByStatus(1L, testAladinBook, stoppedRequestDTO);
        
        // then: 결과 검증
        verify(memberRepository).findById(1L);
        verify(myBookRepository).save(argThat(myBook -> {
            return myBook.getStatus() == BookStatus.STOPPED &&
                    myBook.getMyRating() == 3.0 &&
                    myBook.getOneLineReview().equals("잠시 중단") &&
                    myBook.getCurrentPage() == 30 &&
                    myBook.getStartDate() != null &&
                    myBook.getEndDate() != null;
        }));
        // STOPPED 상태에서는 CalendarStamp가 저장되지 않아야 함
        verify(calendarStampRepository, never()).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("새 책 등록 - 잘못된 상태값으로 등록 시도")
    void insertNewBookByStatus_WhenStatusIsInvalid() {
        // given: 테스트 조건 설정
        when(memberRepository.findById(anyLong())).thenReturn(Optional.of(testMember));
        
        BookStatusRequestDTO invalidRequestDTO = BookStatusRequestDTO.builder()
                .status("INVALID_STATUS")
                .build();
        
        // when & then: 테스트 실행 및 결과 검증
        assertThatThrownBy(() ->
                myBookService.insertNewBookByStatus(1L, testAladinBook, invalidRequestDTO))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("알맞은 status값이 들어와야 합니다");
    }
    
    @Test
    @DisplayName("책 상태 업데이트 - COMPLETED 상태로 변경")
    void updateMyBookStatus_ToCompleted() {
        // given: 테스트 조건 설정
        // 필요한 객체들을 찾을 수 있도록 Mock 설정
        when(myBookRepository.findByIdAndMemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testMyBook));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(testMember));
        
        // COMPLETED 상태로 변경하기 위한 DTO 생성
        BookStatusRequestDTO completedRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.COMPLETED.name())
                .myRating(4.5)                               // 별점 추가
                .oneLineReview("완독했습니다!")               // 한줄평 추가
                .currentPage(testAladinBook.getItemPage()) // 전체 페이지로 설정
                .startDate(LocalDate.now().minusDays(7))     // 일주일 전 시작
                .endDate(LocalDate.now())                    // 오늘 완료
                .build();
        
        // when: 상태 업데이트 메서드 실행
        myBookService.updateMyBookStatus(1L, 1L, completedRequestDTO);
        
        // then: 결과 검증
        // 1. MyBook이 정확한 상태와 데이터로 업데이트되었는지 검증
        verify(myBookRepository).findByIdAndMemberId(1L, 1L);
        
        // 2. Member 조회가 실행되었는지 검증
        verify(memberRepository).findById(1L);
        
        // 3. COMPLETED 상태이므로 CalendarStamp가 생성되었는지 검증
        verify(calendarStampRepository).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("책 상태 업데이트 - READING 상태로 변경")
    void updateMyBookStatus_ToReading() {
        // given: 테스트 조건 설정
        when(myBookRepository.findByIdAndMemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testMyBook));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(testMember));
        
        // READING 상태로 변경하기 위한 DTO 생성
        BookStatusRequestDTO readingRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.READING.name())
                .currentPage(150)                // 현재 읽은 페이지
                .startDate(LocalDate.now())      // 오늘부터 읽기 시작
                .build();
        
        // when: 상태 업데이트 메서드 실행
        myBookService.updateMyBookStatus(1L, 1L, readingRequestDTO);
        
        // then: 결과 검증
        // 1. 필요한 데이터 조회가 실행되었는지 검증
        verify(myBookRepository).findByIdAndMemberId(1L, 1L);
        verify(memberRepository).findById(1L);
        
        // 2. READING 상태이므로 CalendarStamp가 생성되었는지 검증
        verify(calendarStampRepository).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("책 상태 업데이트 - WISH 상태로 변경")
    void updateMyBookStatus_ToWish() {
        // given: 테스트 조건 설정
        when(myBookRepository.findByIdAndMemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testMyBook));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(testMember));
        
        // WISH 상태로 변경하기 위한 DTO 생성
        // WISH는 추가 정보가 필요 없음
        BookStatusRequestDTO wishRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.WISH.name())
                .build();
        
        // when: 상태 업데이트 메서드 실행
        myBookService.updateMyBookStatus(1L, 1L, wishRequestDTO);
        
        // then: 결과 검증
        verify(myBookRepository).findByIdAndMemberId(1L, 1L);
        verify(memberRepository).findById(1L);
        
        // WISH 상태는 CalendarStamp를 생성하지 않음
        verify(calendarStampRepository, never()).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("책 상태 업데이트 - STOPPED 상태로 변경")
    void updateMyBookStatus_ToStopped() {
        // given: 테스트 조건 설정
        when(myBookRepository.findByIdAndMemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testMyBook));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(testMember));
        
        // STOPPED 상태로 변경하기 위한 DTO 생성
        BookStatusRequestDTO stoppedRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.STOPPED.name())
                .currentPage(75)                         // 멈춘 페이지
                .myRating(3.0)                          // 중간 평가
                .oneLineReview("잠시 읽기를 중단합니다")    // 중단 사유
                .startDate(LocalDate.now().minusDays(3)) // 3일 전 시작
                .endDate(LocalDate.now())               // 오늘 중단
                .build();
        
        // when: 상태 업데이트 메서드 실행
        myBookService.updateMyBookStatus(1L, 1L, stoppedRequestDTO);
        
        // then: 결과 검증
        verify(myBookRepository).findByIdAndMemberId(1L, 1L);
        verify(memberRepository).findById(1L);
        
        // STOPPED 상태는 CalendarStamp를 생성하지 않음
        verify(calendarStampRepository, never()).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("책 상태 업데이트 - 잘못된 상태값으로 시도")
    void updateMyBookStatus_WithInvalidStatus() {
        // given: 테스트 조건 설정
        when(myBookRepository.findByIdAndMemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testMyBook));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(testMember));
        
        // 잘못된 상태값을 가진 DTO 생성
        BookStatusRequestDTO invalidRequestDTO = BookStatusRequestDTO.builder()
                .status("INVALID_STATUS")
                .build();
        
        // when & then: 실행 및 검증
        // InvalidStatusException이 발생하는지 확인
        assertThatThrownBy(() ->
                myBookService.updateMyBookStatus(1L, 1L, invalidRequestDTO))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("잘못된 상태값입니다");
    }
    
    @Test
    @DisplayName("책 상태 업데이트 - 필수 데이터 누락")
    void updateMyBookStatus_WithMissingRequiredData() {
        // given: 테스트 조건 설정
        when(myBookRepository.findByIdAndMemberId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testMyBook));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(testMember));
        
        // status가 누락된 DTO 생성
        BookStatusRequestDTO invalidRequestDTO = BookStatusRequestDTO.builder()
                .currentPage(100)
                .build();
        
        // when & then: 실행 및 검증
        assertThatThrownBy(() ->
                myBookService.updateMyBookStatus(1L, 1L, invalidRequestDTO))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessage("상태값은 필수 입력값입니다.");  // 구체적인 에러 메시지 검증
    }
    
    @Nested
    @DisplayName("도서 목록 무한 스크롤 조회 테스트")
    class GetMyBookListTest {
        
        @Test
        @DisplayName("첫 페이지 조회 시 size+1개 가져오는지 확인")
        void getMyBookList_FirstPage() {
            // given
            ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                    .lastId(0L)
                    .size(3)
                    .build();
            
            // Arrays.asList()는 수정 불가능한(immutable) 리스트를 반환하기 때문에 remove() 메서드를 사용할 수 없습니다.
            // 대신 새로운 ArrayList를 생성해야 합니다:
            List<MyBook> mockBooks = new ArrayList<>(Arrays.asList(  // ArrayList로 감싸기
                    createMyBook(5L, "책5"),
                    createMyBook(4L, "책4"),
                    createMyBook(3L, "책3"),
                    createMyBook(2L, "책2") // size + 1 개
            ));
            
            when(myBookRepository.findBooksWithScroll(eq(1L), any(ScrollRequestDTO.class)))
                    .thenReturn(mockBooks);
            
            // when
            ScrollResponseDTO<MyBookResponseDTO> result = myBookService.getMyBookList(1L, requestDTO);
            
            // then
            assertThat(result.getContent()).hasSize(3); // size 만큼만 반환
            assertThat(result.isHasNext()).isTrue(); // 다음 페이지 존재
            assertThat(result.getLastId()).isEqualTo(3L); // 마지막으로 반환된 항목의 ID
            assertThat(result.getNumberOfElements()).isEqualTo(3);
            verify(myBookRepository).findBooksWithScroll(eq(1L), any(ScrollRequestDTO.class));
        }
        
        @Test
        @DisplayName("마지막 페이지 조회 시 남은 데이터만 반환")
        void getMyBookList_LastPage() {
            // given
            ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                    .lastId(3L)
                    .size(3)
                    .build();
            
            List<MyBook> mockBooks = Arrays.asList(
                    createMyBook(2L, "책2"),
                    createMyBook(1L, "책1")
            );
            
            when(myBookRepository.findBooksWithScroll(eq(1L), any(ScrollRequestDTO.class)))
                    .thenReturn(mockBooks);
            
            // when
            ScrollResponseDTO<MyBookResponseDTO> result = myBookService.getMyBookList(1L, requestDTO);
            
            // then
            assertThat(result.getContent()).hasSize(2); // 남은 데이터만큼만 반환
            assertThat(result.isHasNext()).isFalse(); // 다음 페이지 없음
            assertThat(result.getLastId()).isEqualTo(1L);
            assertThat(result.getNumberOfElements()).isEqualTo(2);
        }
        
        @Test
        @DisplayName("검색어와 함께 조회")
        void getMyBookList_WithSearch() {
            // given
            ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                    .lastId(0L)
                    .size(3)
                    .search("특정")
                    .build();
            
            List<MyBook> mockBooks = Arrays.asList(
                    createMyBook(3L, "특정 책3"),
                    createMyBook(2L, "특정 책2"),
                    createMyBook(1L, "특정 책1")
            );
            
            when(myBookRepository.findBooksWithScroll(eq(1L), any(ScrollRequestDTO.class)))
                    .thenReturn(mockBooks);
            
            // when
            ScrollResponseDTO<MyBookResponseDTO> result = myBookService.getMyBookList(1L, requestDTO);
            
            // then
            assertThat(result.getContent())
                    .hasSize(3)
                    .extracting(dto -> dto.getTitle())
                    .allMatch(title -> title.contains("특정"));
            assertThat(result.isHasNext()).isFalse();
        }
        
        @Test
        @DisplayName("빈 결과 조회")
        void getMyBookList_EmptyResult() {
            // given
            ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                    .lastId(0L)
                    .size(3)
                    .search("존재하지않는책")
                    .build();
            
            when(myBookRepository.findBooksWithScroll(eq(1L), any(ScrollRequestDTO.class)))
                    .thenReturn(Collections.emptyList());
            
            // when
            ScrollResponseDTO<MyBookResponseDTO> result = myBookService.getMyBookList(1L, requestDTO);
            
            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.isHasNext()).isFalse();
            assertThat(result.getLastId()).isEqualTo(0L); // 요청한 lastId 그대로 반환
            assertThat(result.getNumberOfElements()).isZero();
        }
        
        // 테스트용 MyBook 객체 생성 헬퍼 메서드
        private MyBook createMyBook(Long id, String title) {
            AladinBook aladinBook = AladinBook.builder()
                    .id(id)
                    .isbn13(String.format("978895674%04d", id))
                    .title(title)
                    .author("테스트 작가")
                    .itemPage(300)
                    .cover("test-cover-url")
                    .link("test-link")
                    .fullDescription("테스트 설명")
                    .publisher("테스트 출판사")
                    .categoryName("IT/컴퓨터")
                    .customerReviewRank(4.5)
                    .build();
            
            return MyBook.builder()
                    .id(id)
                    .member(testMember)
                    .aladinBook(aladinBook)
                    .isbn13(aladinBook.getIsbn13())
                    .status(BookStatus.READING)
                    .build();
        }
    }
    
    @Nested
    @DisplayName("내 책 단건 조회 테스트")
    class GetMyBookTest {
        
        @Test
        @DisplayName("내 책 정상 조회시 DTO로 변환하여 반환")
        void getMyBookSuccess() {
            // given
            //"실제 DB 조회 없이 Optional.of(testMyBook)을 반환해라" 라고 지정하는 것입니다.
            when(myBookRepository.findByIdAndMemberIdWithAladinBook(1L, 1L))
                    .thenReturn(Optional.of(testMyBook));
            // 마찬가지
            when(calendarStampRepository.countByMemberIdAndMyBookId(1L, 1L))
                    .thenReturn(5);
            
            // when
            MyBookResponseDTO result = myBookService.getMyBook(1L, 1L);
            
            // then
            assertThat(result)
                    .extracting(
                            "isbn13",
                            "title",
                            "author",
                            "status",
                            "readDateCount"  // 도장 개수
                    )
                    .containsExactly(
                            "9788956746425",
                            "테스트 책",
                            "테스트 작가",
                            BookStatus.WISH.name(), // dto에는 String 값으로 저장되어 있기 때문에
                            5
                    );
        }
        
        @Test
        @DisplayName("존재하지 않는 책 조회시 예외 발생")
        void getMyBookFail() {
            // given
            when(myBookRepository.findByIdAndMemberIdWithAladinBook(anyLong(), anyLong()))
                    .thenReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> myBookService.getMyBook(1L, 999L))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining("해당 기록된 책을 찾을 수 없습니다.");
        }
    }
    
    @Nested
    @DisplayName("내 책장 기록 삭제  테스트")
    class DeleteMyBookTest {
        
        @Test
        @DisplayName("내 책 삭제 성공 테스트")
        void testDeleteMyBook() {
            // given
            Long memberId = 1L;
            Long bookId = 1L;
            
            MyBook myBook = MyBook.builder()
                    .id(bookId)
                    .member(Member.builder().id(memberId).build())
                    .build();
            
            when(myBookRepository.findByIdAndMemberId(bookId, memberId)) // 이 메소드가 호출되면
                    .thenReturn(Optional.of(myBook));                   // myBook을 반환하도록 설정
            
            // when
            myBookService.deleteMyBook(memberId, bookId); // 실제 삭제 메소드 호출
            
            // then
            // 1. findByIdAndMemberId 메소드가 정확히 한 번 호출되었는지 검증
            // - bookId와 memberId 파라미터로 호출되었는지 확인
            verify(myBookRepository).findByIdAndMemberId(bookId, memberId);
            // 2. deleteByIdAndMemberId 메소드가 정확히 한 번 호출되었는지 검증
            // - bookId와 memberId 파라미터로 호출되었는지 확인
            verify(myBookRepository).deleteByIdAndMemberId(bookId, memberId);
            
            // 참고: verify()로 할 수 있는 다른 검증들
//            verify(myBookRepository, times(1)).findByIdAndMemberId(bookId, memberId);  // 정확히 1번 호출
//            verify(myBookRepository, never()).otherMethod();  // 이 메소드는 절대 호출되지 않았어야 함 otherMethod는 예시
//            verify(myBookRepository, atLeastOnce()).findByIdAndMemberId(bookId, memberId);  // 최소 1번 이상 호출
            // 호출 순서도 검증 가능
            //InOrder inOrder = inOrder(myBookRepository);
            //inOrder.verify(myBookRepository).findByIdAndMemberId(bookId, memberId);
            //inOrder.verify(myBookRepository).deleteByIdAndMemberId(bookId, memberId);
        }
        
        @Test
        @DisplayName("존재하지 않는 책 삭제 시도시 실패")
        void deleteMyBookFail_NotFound() {
            // given
            Long memberId = 1L;
            Long invalidBookId = 999L;
            
            when(myBookRepository.findByIdAndMemberId(invalidBookId, memberId))
                    .thenReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> myBookService.deleteMyBook(memberId, invalidBookId))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining("해당 기록된 책을 찾을 수 없습니다.");
            
            verify(myBookRepository).findByIdAndMemberId(invalidBookId, memberId);
            verify(myBookRepository, never()).deleteByIdAndMemberId(any(), any());
        }
        
        @Test
        @DisplayName("다른 사용자의 책 삭제 시도시 실패")
        void deleteMyBookFail_WrongUser() {
            // given
            Long memberId = 1L;
            Long wrongMemberId = 2L;
            Long bookId = 1L;
            
            when(myBookRepository.findByIdAndMemberId(bookId, wrongMemberId))
                    .thenReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> myBookService.deleteMyBook(wrongMemberId, bookId))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining("해당 기록된 책을 찾을 수 없습니다.");
            
            verify(myBookRepository).findByIdAndMemberId(bookId, wrongMemberId);
            verify(myBookRepository, never()).deleteByIdAndMemberId(any(), any());
        }
    }
    
    @Nested
    @DisplayName("다른 회원의 도서 목록 무한 스크롤 조회 테스트")
    class GetOtherMemberBookListTest {
        
        @Test
        @DisplayName("로그인한 회원이 동일한 책을 가지고 있는 경우")
        void getOtherMemberBookList_WithLoginMemberHavingSameBook() {
            // given
            Long loginMemberId = 1L;
            Long targetMemberId = 2L;
            ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                    .lastId(0L)
                    .size(3)
                    .build();
            
            // 다른 회원의 책 목록 생성
            List<MyBook> otherMemberBooks = new ArrayList<>(Arrays.asList(
                    createMyBook(5L, "책5", "12345", BookStatus.READING),  // COMPLETED -> READING으로 변경 (다른 회원의 상태)
                    createMyBook(4L, "책4", "12344", BookStatus.READING),  // null -> READING으로 변경 (status는 null이 될 수 없음)
                    createMyBook(3L, "책3", "12343", BookStatus.READING)   // null -> READING으로 변경 (status는 null이 될 수 없음)
            ));
            
            // 로그인한 회원이 가진 책 생성 (isbn13이 "12345"인 책을 가지고 있음)
            MyBook loginMemberBook = createMyBook(6L, "책5", "12345", BookStatus.COMPLETED);
            Set<MyBook> loginMemberBooks = new HashSet<>(Collections.singletonList(loginMemberBook));
            
            when(myBookRepository.findBooksWithScroll(eq(targetMemberId), any(ScrollRequestDTO.class)))
                    .thenReturn(otherMemberBooks);
            when(myBookRepository.findByMemberId(loginMemberId))
                    .thenReturn(loginMemberBooks);
            when(calendarStampRepository.countByMemberIdAndMyBookId(eq(targetMemberId), anyLong()))
                    .thenReturn(3);
            
            // when
            ScrollResponseDTO<MyBookResponseDTO> result = myBookService.getOtherMemberBookList(loginMemberId,
                    targetMemberId, requestDTO);
            
            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.isHasNext()).isFalse();
            
            // 첫 번째 책(isbn13: "12345")은 타겟 회원의 상태값을 가져야 함
            MyBookResponseDTO firstBook = result.getContent().get(0);
            assertThat(firstBook.getStatus()).isEqualTo(BookStatus.READING.name());
            
            // 나머지 책들은 다른 회원의 상태값(READING)이어야 함
            assertThat(result.getContent().get(1).getStatus()).isEqualTo(
                    BookStatus.READING.name());  // null -> READING으로 변경
            assertThat(result.getContent().get(2).getStatus()).isEqualTo(
                    BookStatus.READING.name());  // null -> READING으로 변경
        }
        
        @Test
        @DisplayName("로그인한 회원이 동일한 책을 가지고 있지 않은 경우")
        void getOtherMemberBookList_WithoutLoginMemberHavingSameBook() {
            // given
            Long loginMemberId = 1L;
            Long targetMemberId = 2L;
            ScrollRequestDTO requestDTO = ScrollRequestDTO.builder()
                    .lastId(0L)
                    .size(3)
                    .build();
            
            List<MyBook> otherMemberBooks = new ArrayList<>(Arrays.asList(
                    createMyBook(5L, "책5", "12345", BookStatus.READING),  // null -> READING으로 변경
                    createMyBook(4L, "책4", "12344", BookStatus.READING),  // null -> READING으로 변경
                    createMyBook(3L, "책3", "12343", BookStatus.READING)   // null -> READING으로 변경
            ));
            
            // 로그인한 회원은 다른 책을 가지고 있음
            MyBook loginMemberBook = createMyBook(6L, "다른책", "99999");
            Set<MyBook> loginMemberBooks = new HashSet<>(Collections.singletonList(loginMemberBook));
            
            when(myBookRepository.findBooksWithScroll(eq(targetMemberId), any(ScrollRequestDTO.class)))
                    .thenReturn(otherMemberBooks);
            when(myBookRepository.findByMemberId(loginMemberId))
                    .thenReturn(loginMemberBooks);
            when(calendarStampRepository.countByMemberIdAndMyBookId(eq(targetMemberId), anyLong()))
                    .thenReturn(3);
            
            // when
            ScrollResponseDTO<MyBookResponseDTO> result = myBookService.getOtherMemberBookList(loginMemberId,
                    targetMemberId, requestDTO);
            
            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.isHasNext()).isFalse();
            
            // 모든 책에 다른 회원의 상태값(READING)이 있어야 함
            assertThat(result.getContent())
                    .extracting(MyBookResponseDTO::getStatus)
                    .containsOnly(BookStatus.READING.name());  // containsOnlyNulls()
        }
        
        // ISBN을 지정할 수 있도록 createMyBook 메서드 수정
        private MyBook createMyBook(Long id, String title, String isbn13) {
            return createMyBook(id, title, isbn13, BookStatus.READING);  // null -> BookStatus.READING으로 변경
        }
        
        private MyBook createMyBook(Long id, String title, String isbn13, BookStatus bookStatus) {
            AladinBook aladinBook = AladinBook.builder()
                    .id(id)
                    .isbn13(isbn13)
                    .title(title)
                    .author("테스트 작가")
                    .itemPage(300)
                    .cover("test-cover-url")
                    .link("test-link")
                    .fullDescription("테스트 설명")
                    .publisher("테스트 출판사")
                    .categoryName("IT/컴퓨터")
                    .customerReviewRank(4.5)
                    .build();
            
            return MyBook.builder()
                    .id(id)
                    .member(testMember)
                    .aladinBook(aladinBook)
                    .isbn13(isbn13)
                    .status(bookStatus)
                    .build();
        }
    }
    
    @Nested
    @DisplayName("다른 회원의 도서 단건 조회 테스트")
    class GetOtherMemberBookTest {
        
        @Test
        @DisplayName("다른 회원의 도서를 정상적으로 조회하고 로그인한 회원이 같은 ISBN의 책을 가지고 있는 경우")
        void getOtherMemberBook_WithLoginMemberHavingSameIsbn() {
            // given
            Long loginMemberId = 1L;
            Long targetMemberId = 2L;
            Long myBookId = 1L;
            String isbn13 = "12345";
            
            // 대상 회원의 책 설정
            MyBook targetMemberBook = createMyBook(myBookId, "테스트 책", isbn13, BookStatus.READING);
            
            // 로그인한 회원의 같은 ISBN을 가진 책 설정
            MyBook loginMemberBook = createMyBook(2L, "테스트 책", isbn13, BookStatus.COMPLETED);
            
            when(myBookRepository.findByIdAndMemberIdWithAladinBook(myBookId, targetMemberId))
                    .thenReturn(Optional.of(targetMemberBook));
            when(calendarStampRepository.countByMemberIdAndMyBookId(targetMemberId, myBookId))
                    .thenReturn(5);
            when(myBookRepository.findByMemberIdAndIsbn13(loginMemberId, isbn13))
                    .thenReturn(Optional.of(loginMemberBook));
            
            // when
            MyBookResponseDTO result = myBookService.getOtherMemberBook(loginMemberId, targetMemberId, myBookId);
            
            // then
            assertThat(result)
                    .extracting("isbn13", "title", "author", "status", "readDateCount", "existingStatus")
                    .containsExactly(isbn13, "테스트 책", "테스트 작가",
                            BookStatus.READING.name(), 5, BookStatus.COMPLETED.name());
            
            verify(myBookRepository).findByIdAndMemberIdWithAladinBook(myBookId, targetMemberId);
            verify(calendarStampRepository).countByMemberIdAndMyBookId(targetMemberId, myBookId);
            verify(myBookRepository).findByMemberIdAndIsbn13(loginMemberId, isbn13);
        }
        
        @Test
        @DisplayName("다른 회원의 도서를 정상적으로 조회하고 로그인한 회원이 책을 가지고 있지 않은 경우")
        void getOtherMemberBook_WithoutLoginMemberBook() {
            // given
            Long loginMemberId = 1L;
            Long targetMemberId = 2L;
            Long myBookId = 1L;
            String isbn13 = "12345";
            
            MyBook targetMemberBook = createMyBook(myBookId, "테스트 책", isbn13, BookStatus.READING);
            
            when(myBookRepository.findByIdAndMemberIdWithAladinBook(myBookId, targetMemberId))
                    .thenReturn(Optional.of(targetMemberBook));
            when(calendarStampRepository.countByMemberIdAndMyBookId(targetMemberId, myBookId))
                    .thenReturn(5);
            when(myBookRepository.findByMemberIdAndIsbn13(loginMemberId, isbn13))
                    .thenReturn(Optional.empty());
            
            // when
            MyBookResponseDTO result = myBookService.getOtherMemberBook(loginMemberId, targetMemberId, myBookId);
            
            // then
            assertThat(result)
                    .extracting("isbn13", "title", "author", "status", "readDateCount", "existingStatus")
                    .containsExactly(isbn13, "테스트 책", "테스트 작가",
                            BookStatus.READING.name(), 5, null);
            
            verify(myBookRepository).findByIdAndMemberIdWithAladinBook(myBookId, targetMemberId);
            verify(calendarStampRepository).countByMemberIdAndMyBookId(targetMemberId, myBookId);
            verify(myBookRepository).findByMemberIdAndIsbn13(loginMemberId, isbn13);
        }
        
        @Test
        @DisplayName("존재하지 않는 도서 조회 시 예외 발생")
        void getOtherMemberBook_NotFound() {
            // given
            Long loginMemberId = 1L;
            Long targetMemberId = 2L;
            Long invalidBookId = 999L;
            
            when(myBookRepository.findByIdAndMemberIdWithAladinBook(invalidBookId, targetMemberId))
                    .thenReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() ->
                    myBookService.getOtherMemberBook(loginMemberId, targetMemberId, invalidBookId))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining("해당 기록된 책을 찾을 수 없습니다");
            
            verify(myBookRepository).findByIdAndMemberIdWithAladinBook(invalidBookId, targetMemberId);
            verify(calendarStampRepository, never()).countByMemberIdAndMyBookId(anyLong(), anyLong());
            verify(myBookRepository, never()).findByMemberIdAndIsbn13(anyLong(), anyString());
        }
    }
    
    // 테스트에 필요한 MyBook 객체 생성 헬퍼 메소드
    private MyBook createMyBook(Long id, String title, String isbn13, BookStatus status) {
        AladinBook aladinBook = AladinBook.builder()
                .id(id)
                .isbn13(isbn13)
                .title(title)
                .author("테스트 작가")
                .itemPage(300)
                .cover("test-cover-url")
                .link("test-link")
                .fullDescription("테스트 설명")
                .publisher("테스트 출판사")
                .categoryName("IT/컴퓨터")
                .customerReviewRank(4.5)
                .build();
        
        return MyBook.builder()
                .id(id)
                .member(testMember)
                .aladinBook(aladinBook)
                .isbn13(isbn13)
                .status(status)
                .build();
    }
    
    @Nested
    @DisplayName("다른 회원의 책을 WISH 상태로 등록하는 테스트")
    class InsertNewBookByWishTest {
        
        @Test
        @DisplayName("다른 회원의 책을 정상적으로 WISH 상태로 등록")
        void insertNewBookByWish_Success() {
            // given
            Long memberId = 1L;
            Long targetMyBookId = 2L;
            String isbn13 = "9788956746425";
            
            Member loginMember = Member.builder()
                    .id(memberId)
                    .username("test@test.com")
                    .build();
            
            AladinBook aladinBook = AladinBook.builder()
                    .id(1L)
                    .isbn13(isbn13)
                    .title("테스트 책")
                    .author("테스트 작가")
                    .itemPage(300)
                    .cover("test-cover-url")
                    .build();
            
            MyBook targetMyBook = MyBook.builder()
                    .id(targetMyBookId)
                    .member(Member.builder().id(2L).build())
                    .aladinBook(aladinBook)
                    .isbn13(isbn13)
                    .status(BookStatus.READING)
                    .build();
            
            when(myBookRepository.findById(targetMyBookId)).thenReturn(Optional.of(targetMyBook));
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(loginMember));
            when(myBookRepository.findByMemberIdAndIsbn13(memberId, isbn13))
                    .thenReturn(Optional.empty());
            
            // when
            myBookService.insertNewBookByWish(memberId, targetMyBookId);
            
            // then
            verify(myBookRepository).findById(targetMyBookId);
            verify(memberRepository).findById(memberId);
            verify(myBookRepository).findByMemberIdAndIsbn13(memberId, isbn13);
            verify(myBookRepository).save(argThat(myBook ->
                    myBook.getMember().getId().equals(memberId) &&
                            myBook.getAladinBook().equals(aladinBook) &&
                            myBook.getIsbn13().equals(isbn13) &&
                            myBook.getStatus() == BookStatus.WISH
            ));
        }
        
        @Test
        @DisplayName("이미 동일한 ISBN의 책을 가지고 있는 경우 예외 발생")
        void insertNewBookByWish_DuplicateBook() {
            // given
            Long memberId = 1L;
            Long targetMyBookId = 2L;
            String isbn13 = "9788956746425";
            
            Member loginMember = Member.builder()
                    .id(memberId)
                    .username("test@test.com")
                    .build();
            
            MyBook targetMyBook = MyBook.builder()
                    .id(targetMyBookId)
                    .aladinBook(testAladinBook)
                    .isbn13(isbn13)
                    .status(BookStatus.READING)
                    .build();
            
            MyBook existingBook = MyBook.builder()
                    .id(3L)  // 다른 ID지만 같은 ISBN
                    .member(loginMember)
                    .aladinBook(testAladinBook)
                    .isbn13(isbn13)
                    .status(BookStatus.WISH)
                    .build();
            
            when(myBookRepository.findById(targetMyBookId)).thenReturn(Optional.of(targetMyBook));
            when(memberRepository.findById(memberId)).thenReturn(Optional.of(loginMember)); // 이 부분 추가
            when(myBookRepository.findByMemberIdAndIsbn13(memberId, isbn13))
                    .thenReturn(Optional.of(existingBook));
            
            // when & then
            assertThatThrownBy(() -> myBookService.insertNewBookByWish(memberId, targetMyBookId))
                    .isInstanceOf(BookDuplicateNotAllowException.class)
                    .hasMessageContaining("MyBook은 중복 저장할 수 없습니다.");
            
            verify(myBookRepository).findById(targetMyBookId);
            verify(memberRepository).findById(memberId);  // 이 부분 추가
            verify(myBookRepository).findByMemberIdAndIsbn13(memberId, isbn13);
            verify(myBookRepository, never()).save(any(MyBook.class));
        }
        
        @Test
        @DisplayName("존재하지 않는 책 ID로 등록 시도할 경우 예외 발생")
        void insertNewBookByWish_BookNotFound() {
            // given
            Long memberId = 1L;
            Long invalidBookId = 999L;
            
            when(myBookRepository.findById(invalidBookId))
                    .thenReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> myBookService.insertNewBookByWish(memberId, invalidBookId))
                    .isInstanceOf(BookNotFoundException.class)
                    .hasMessageContaining(String.format("해당 기록된 책을 찾을 수 없습니다.", invalidBookId));
            
            verify(myBookRepository).findById(invalidBookId);
            verify(memberRepository, never()).findById(anyLong());
            verify(myBookRepository, never()).findByMemberIdAndIsbn13(anyLong(), anyString());
            verify(myBookRepository, never()).save(any(MyBook.class));
        }
        
        @Test
        @DisplayName("존재하지 않는 회원 ID로 등록 시도할 경우 예외 발생")
        void insertNewBookByWish_MemberNotFound() {
            // given
            Long invalidMemberId = 999L;
            Long targetMyBookId = 2L;
            
            MyBook targetMyBook = MyBook.builder()
                    .id(targetMyBookId)
                    .member(Member.builder().id(2L).build())
                    .aladinBook(testAladinBook)
                    .isbn13("9788956746425")
                    .status(BookStatus.READING)
                    .build();
            
            when(myBookRepository.findById(targetMyBookId)).thenReturn(Optional.of(targetMyBook));
            when(memberRepository.findById(invalidMemberId)).thenReturn(Optional.empty());
            
            // when & then
            assertThatThrownBy(() -> myBookService.insertNewBookByWish(invalidMemberId, targetMyBookId))
                    .isInstanceOf(MemberException.class)
                    .hasMessageContaining(String.format("해당 member를 찾을 수 없습니다.", invalidMemberId));
            
            verify(myBookRepository).findById(targetMyBookId);
            verify(memberRepository).findById(invalidMemberId);
            verify(myBookRepository, never()).findByMemberIdAndIsbn13(anyLong(), anyString());
            verify(myBookRepository, never()).save(any(MyBook.class));
        }
        
        @Test
        @DisplayName("AladinBook이 null인 경우 예외 발생")
        void insertNewBookByWish_AladinBookNotFound() {
            // given
            Long memberId = 1L;
            Long targetMyBookId = 2L;
            
            MyBook targetMyBook = MyBook.builder()
                    .id(targetMyBookId)
                    .member(Member.builder().id(2L).build())
                    .aladinBook(null)  // AladinBook을 null로 설정
                    .isbn13("9788956746425")
                    .status(BookStatus.READING)
                    .build();
            
            when(myBookRepository.findById(targetMyBookId)).thenReturn(Optional.of(targetMyBook));
            
            // when & then
            assertThatThrownBy(() -> myBookService.insertNewBookByWish(memberId, targetMyBookId))
                    .isInstanceOf(AladinBookNotFoundException.class)
                    .hasMessageContaining("해당 기록된 알라딘 책을 찾을 수 없습니다.");
            
            verify(myBookRepository).findById(targetMyBookId);
            verify(memberRepository, never()).findById(anyLong());
            verify(myBookRepository, never()).findByMemberIdAndIsbn13(anyLong(), anyString());
            verify(myBookRepository, never()).save(any(MyBook.class));
        }
    }
}