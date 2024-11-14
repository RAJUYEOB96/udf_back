package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.CalendarStamp;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.dto.request.book.BookStatusRequestDTO;
import com.undefinedus.backend.exception.book.BookException;
import com.undefinedus.backend.exception.book.InvalidStatusException;
import com.undefinedus.backend.exception.member.MemberException;
import com.undefinedus.backend.repository.CalendarStampRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
@Transactional
public class MyBookServiceImpl implements MyBookService {
    
    // === 에러 메시지 상수 === //
    private static final String MEMBER_NOT_FOUND = "해당 member를 찾을 수 없습니다. : %d";
    private static final String USER_NOT_FOUND = "해당 유저를 찾을 수 없습니다. : %d";
    private static final String BOOK_NOT_FOUND = "해당 기록된 책을 찾을 수 없습니다. : 멤버 id - %d, 책 id - %d";
    private static final String INVALID_STATUS = "알맞은 status값이 들어와야 합니다. : %s";
    private static final String USER_AUTH_FAILED = "사용자를 찾을 수 없어 책 등록에 실패했습니다.";
    private static final String BOOK_REGISTRATION_FAILED = "책 등록 처리 중 오류가 발생했습니다.";
    private static final String CALENDAR_STAMP_ERROR = "독서 기록 저장 중 오류가 발생했습니다.";
    
    // === Repository 주입 === //
    private final MyBookRepository myBookRepository;
    private final MemberRepository memberRepository;
    private final CalendarStampRepository calendarStampRepository;
    
    
    @Override
    public boolean existsBook(Long memberId, String isbn13) {
        
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(String.format(MEMBER_NOT_FOUND, memberId)));
        
        return myBookRepository.findByMemberIdAndIsbn13(member.getId(), isbn13).isPresent();
        
    }
    
    @Override
    public void insertNewBookByStatus(Long memberId, AladinBook savedAladinBook, BookStatusRequestDTO requestDTO) {
        try {
            
            Member findMember = getLoginMember(memberId);
            
            MyBook myBook = MyBook.builder()
                    .member(findMember)
                    .aladinBook(savedAladinBook)
                    .isbn13(savedAladinBook.getIsbn13())
                    .build();
            
            // TODO : 나중 QueryDSL 적용하면 바꿀지 말지 고민필요
            saveBookAndCalenarStampByStatus(requestDTO, myBook, savedAladinBook, findMember);
        } catch (MemberException e) {
            log.error("사용자를 찾을 수 없습니다. ID: {}", memberId, e);
            throw new MemberException(USER_AUTH_FAILED, e);
        } catch (InvalidStatusException e) {  // InvalidStatusException을 별도로 처리
            log.error("잘못된 상태값입니다. status: {}", requestDTO.getStatus(), e);
            throw e;  // 그대로 전파
        } catch (Exception e) {
            log.error("책 등록 중 에러가 발생했습니다. ID: {}", memberId, e);
            throw new BookException(BOOK_REGISTRATION_FAILED, e);
        }
        
    }
    
    @Override
    public void updateBookStatus(Long memberId, Long bookId, BookStatusRequestDTO requestDTO) {
        MyBook findMyBook = myBookRepository.findByMemberIdAndId(memberId, bookId)
                .orElseThrow(() -> new BookException(
                        String.format(BOOK_NOT_FOUND, memberId, bookId)));
        
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(String.format(USER_NOT_FOUND, memberId)));
        
        // 직접 엔티티의 값을 변경
        // 더티 체킹으로 자동 업데이트
        findMyBook.updateStatus(requestDTO);
        
        // 업데이트 할때 마다 달력 스탬프에 기록 (읽고 있는중, 다 읽은 책 만)
        if (BookStatus.READING.name().equals(requestDTO.getStatus()) || BookStatus.COMPLETED.name()
                .equals(requestDTO.getStatus())) {
            recordCalendarStamp(findMember, findMyBook);
        }
    }
    
    private void saveBookAndCalenarStampByStatus(
            BookStatusRequestDTO requestDTO, MyBook myBook,
            AladinBook findAladinBook, Member findMember) {
        
        // 업데이트 할때 마다 달력 스탬프에 기록 (읽고 있는중, 다 읽은 책 만)
        if (BookStatus.COMPLETED.name().equals(requestDTO.getStatus())) {
            MyBook savedMyBook = saveBookWithCompletedStatus(requestDTO, myBook, findAladinBook);
            recordCalendarStamp(findMember, savedMyBook);
        } else if (BookStatus.READING.name().equals(requestDTO.getStatus())) {
            MyBook savedMyBook = saveBookWithReadingStatus(requestDTO, myBook);
            recordCalendarStamp(findMember, savedMyBook);
        } else if (BookStatus.WISH.name().equals(requestDTO.getStatus())) {
            saveBookWithWishStatus(myBook);
        } else if (BookStatus.STOPPED.name().equals(requestDTO.getStatus())) {
            saveBookWithStoppedStatus(requestDTO, myBook);
        } else {
            throw new InvalidStatusException(String.format(INVALID_STATUS, requestDTO.getStatus()));
        }
    }
    
    private void saveBookWithStoppedStatus(BookStatusRequestDTO requestDTO, MyBook myBook) {
        myBook = myBook.toBuilder()
                .status(BookStatus.STOPPED)
                .myRating(requestDTO.getMyRating())
                .oneLineReview(requestDTO.getOneLineReview())
                .currentPage(requestDTO.getCurrentPage())
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .build();
        
        myBookRepository.save(myBook);
    }
    
    private void saveBookWithWishStatus(MyBook myBook) {
        myBook = myBook.toBuilder()
                .status(BookStatus.WISH)
                .build();
        
        myBookRepository.save(myBook);
    }
    
    private MyBook saveBookWithReadingStatus(BookStatusRequestDTO requestDTO, MyBook myBook) {
        myBook = myBook.toBuilder()
                .status(BookStatus.READING)
                .myRating(requestDTO.getMyRating())
                .currentPage(requestDTO.getCurrentPage())
                .startDate(LocalDate.now())
                .build();
        
        return myBookRepository.save(myBook);
    }
    
    private MyBook saveBookWithCompletedStatus(BookStatusRequestDTO requestDTO, MyBook myBook,
            AladinBook findAladinBook) {
        myBook = myBook.toBuilder()
                .status(BookStatus.COMPLETED)
                .myRating(requestDTO.getMyRating())
                .oneLineReview(requestDTO.getOneLineReview())
                .currentPage(findAladinBook.getPagesCount()) // 다 읽었으니 100%로 만들기 위해
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .build();
        
        return myBookRepository.save(myBook);
    }
    
    private void recordCalendarStamp(Member findMember, MyBook myBook) {
        try {
            CalendarStamp calendarStamp = CalendarStamp.builder()
                    .member(findMember)
                    .myBook(myBook)
                    .bookCoverUrl(myBook.getAladinBook().getCover())
                    .recordedAt(LocalDate.now())
                    .status(myBook.getStatus())
                    .build();
            
            calendarStampRepository.save(calendarStamp);
        } catch (Exception e) {
            log.error("캘린더 스탬프 기록 중 오류 발생. MyBook ID: {}", myBook.getId(), e);
            throw new BookException(CALENDAR_STAMP_ERROR, e);
        }
        
    }
    
    private Member getLoginMember(Long memberId) {
        
        Member findMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(String.format(MEMBER_NOT_FOUND, memberId)));
        
        log.info("찾아온 member 정보: {}", findMember);
        
        return findMember;
    }
}
