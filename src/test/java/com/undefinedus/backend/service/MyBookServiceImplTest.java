package com.undefinedus.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.CalendarStamp;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.dto.request.book.BookStatusRequestDTO;
import com.undefinedus.backend.exception.book.InvalidStatusException;
import com.undefinedus.backend.repository.CalendarStampRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
                .pagesCount(300)
                .cover("test-cover-url")
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
        
        BookStatusRequestDTO completedRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.COMPLETED.name())
                .myRating(4.5)
                .oneLineReview("좋은 책이었습니다.")
                .currentPage(testAladinBook.getPagesCount())
                .startDate(LocalDate.now().minusDays(7))
                .endDate(LocalDate.now())
                .build();
        
        // when: 테스트할 메서드 실행
        myBookService.insertNewBookByStatus(1L,  testAladinBook, completedRequestDTO);
        
        // then: 결과 검증
        verify(memberRepository).findById(1L);
        verify(myBookRepository).save(argThat(myBook -> {
            return myBook.getStatus() == BookStatus.COMPLETED &&
                    myBook.getMyRating() == 4.5 &&
                    myBook.getOneLineReview().equals("좋은 책이었습니다.") &&
                    myBook.getCurrentPage() == testAladinBook.getPagesCount() &&
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
    void updateBookStatus_ToCompleted() {
        // given: 테스트 조건 설정
        // 필요한 객체들을 찾을 수 있도록 Mock 설정
        when(myBookRepository.findByMemberIdAndId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testMyBook));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(testMember));
        
        // COMPLETED 상태로 변경하기 위한 DTO 생성
        BookStatusRequestDTO completedRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.COMPLETED.name())
                .myRating(4.5)                               // 별점 추가
                .oneLineReview("완독했습니다!")               // 한줄평 추가
                .currentPage(testAladinBook.getPagesCount()) // 전체 페이지로 설정
                .startDate(LocalDate.now().minusDays(7))     // 일주일 전 시작
                .endDate(LocalDate.now())                    // 오늘 완료
                .build();
        
        // when: 상태 업데이트 메서드 실행
        myBookService.updateBookStatus(1L, 1L, completedRequestDTO);
        
        // then: 결과 검증
        // 1. MyBook이 정확한 상태와 데이터로 업데이트되었는지 검증
        verify(myBookRepository).findByMemberIdAndId(1L, 1L);
        
        // 2. Member 조회가 실행되었는지 검증
        verify(memberRepository).findById(1L);
        
        // 3. COMPLETED 상태이므로 CalendarStamp가 생성되었는지 검증
        verify(calendarStampRepository).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("책 상태 업데이트 - READING 상태로 변경")
    void updateBookStatus_ToReading() {
        // given: 테스트 조건 설정
        when(myBookRepository.findByMemberIdAndId(anyLong(), anyLong()))
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
        myBookService.updateBookStatus(1L, 1L, readingRequestDTO);
        
        // then: 결과 검증
        // 1. 필요한 데이터 조회가 실행되었는지 검증
        verify(myBookRepository).findByMemberIdAndId(1L, 1L);
        verify(memberRepository).findById(1L);
        
        // 2. READING 상태이므로 CalendarStamp가 생성되었는지 검증
        verify(calendarStampRepository).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("책 상태 업데이트 - WISH 상태로 변경")
    void updateBookStatus_ToWish() {
        // given: 테스트 조건 설정
        when(myBookRepository.findByMemberIdAndId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testMyBook));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(testMember));
        
        // WISH 상태로 변경하기 위한 DTO 생성
        // WISH는 추가 정보가 필요 없음
        BookStatusRequestDTO wishRequestDTO = BookStatusRequestDTO.builder()
                .status(BookStatus.WISH.name())
                .build();
        
        // when: 상태 업데이트 메서드 실행
        myBookService.updateBookStatus(1L, 1L, wishRequestDTO);
        
        // then: 결과 검증
        verify(myBookRepository).findByMemberIdAndId(1L, 1L);
        verify(memberRepository).findById(1L);
        
        // WISH 상태는 CalendarStamp를 생성하지 않음
        verify(calendarStampRepository, never()).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("책 상태 업데이트 - STOPPED 상태로 변경")
    void updateBookStatus_ToStopped() {
        // given: 테스트 조건 설정
        when(myBookRepository.findByMemberIdAndId(anyLong(), anyLong()))
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
        myBookService.updateBookStatus(1L, 1L, stoppedRequestDTO);
        
        // then: 결과 검증
        verify(myBookRepository).findByMemberIdAndId(1L, 1L);
        verify(memberRepository).findById(1L);
        
        // STOPPED 상태는 CalendarStamp를 생성하지 않음
        verify(calendarStampRepository, never()).save(any(CalendarStamp.class));
    }
    
    @Test
    @DisplayName("책 상태 업데이트 - 잘못된 상태값으로 시도")
    void updateBookStatus_WithInvalidStatus() {
        // given: 테스트 조건 설정
        when(myBookRepository.findByMemberIdAndId(anyLong(), anyLong()))
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
                myBookService.updateBookStatus(1L, 1L, invalidRequestDTO))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessageContaining("잘못된 상태값입니다");    }
    
    @Test
    @DisplayName("책 상태 업데이트 - 필수 데이터 누락")
    void updateBookStatus_WithMissingRequiredData() {
        // given: 테스트 조건 설정
        when(myBookRepository.findByMemberIdAndId(anyLong(), anyLong()))
                .thenReturn(Optional.of(testMyBook));
        when(memberRepository.findById(anyLong()))
                .thenReturn(Optional.of(testMember));
        
        // status가 누락된 DTO 생성
        BookStatusRequestDTO invalidRequestDTO = BookStatusRequestDTO.builder()
                .currentPage(100)
                .build();
        
        // when & then: 실행 및 검증
        assertThatThrownBy(() ->
                myBookService.updateBookStatus(1L, 1L, invalidRequestDTO))
                .isInstanceOf(InvalidStatusException.class)
                .hasMessage("상태값은 필수 입력값입니다.");  // 구체적인 에러 메시지 검증
    }
}