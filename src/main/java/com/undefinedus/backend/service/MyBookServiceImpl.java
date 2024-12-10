package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.CalendarStamp;
import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.dto.request.ScrollRequestDTO;
import com.undefinedus.backend.dto.request.book.BookStatusRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.book.MyBookResponseDTO;
import com.undefinedus.backend.exception.aladinBook.AladinBookNotFoundException;
import com.undefinedus.backend.exception.book.BookDuplicateNotAllowException;
import com.undefinedus.backend.exception.book.BookException;
import com.undefinedus.backend.exception.book.BookNotFoundException;
import com.undefinedus.backend.exception.book.InvalidStatusException;
import com.undefinedus.backend.exception.discussion.DiscussionException;
import com.undefinedus.backend.exception.member.MemberException;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.repository.CalendarStampRepository;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
    private static final String ALADIN_BOOK_NOT_FOUND = "해당 기록된 알라딘 책을 찾을 수 없습니다.";
    private static final String USER_NOT_FOUND = "해당 유저를 찾을 수 없습니다. : %d";
    private static final String BOOK_NOT_FOUND_BY_ID = "해당 기록된 책을 찾을 수 없습니다. : 책 id - %d";
    private static final String BOOK_CAN_NOT_DUPLICATE = "MyBook은 중복 저장할 수 없습니다.";
    private static final String BOOK_NOT_FOUND = "해당 기록된 책을 찾을 수 없습니다. : 멤버 id - %d, 책 id - %d";
    private static final String INVALID_STATUS = "알맞은 status값이 들어와야 합니다. : %s";
    private static final String USER_AUTH_FAILED = "사용자를 찾을 수 없어 책 등록에 실패했습니다.";
    private static final String BOOK_REGISTRATION_FAILED = "책 등록 처리 중 오류가 발생했습니다.";
    private static final String CALENDAR_STAMP_ERROR = "독서 기록 저장 중 오류가 발생했습니다.";

    // === Repository 주입 === //
    private final MyBookRepository myBookRepository;
    private final MemberRepository memberRepository;
    private final CalendarStampRepository calendarStampRepository;
    private final DiscussionRepository discussionRepository;


    @Override
    public boolean existsBook(Long memberId, String isbn13) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberException(String.format(MEMBER_NOT_FOUND, memberId)));
        
        Optional<MyBook> byMemberIdAndIsbn13 = myBookRepository.findByMemberIdAndIsbn13(member.getId(), isbn13);
        
        if (byMemberIdAndIsbn13.isPresent()) {
            throw new BookDuplicateNotAllowException(BOOK_CAN_NOT_DUPLICATE);
        }
        return false;
    }

    @Override
    public Long insertNewBookByStatus(Long memberId, AladinBook savedAladinBook,
        BookStatusRequestDTO requestDTO) {
        
        Optional<MyBook> check = myBookRepository.findByMemberIdAndIsbn13(memberId, savedAladinBook.getIsbn13());
        
        if (check.isPresent()) {
            throw new BookDuplicateNotAllowException(BOOK_CAN_NOT_DUPLICATE);
        } else {
            log.info("myBook is Null");
        }
        
        Member findMember = getLoginMember(memberId);

        MyBook myBook = MyBook.builder()
            .member(findMember)
            .aladinBook(savedAladinBook)
            .isbn13(savedAladinBook.getIsbn13())
            .build();
        
        return saveBookAndCalenarStampByStatus(requestDTO, myBook, savedAladinBook, findMember);
    }

    @Override
    public Long updateMyBookStatus(Long memberId, Long bookId, BookStatusRequestDTO requestDTO) {
        MyBook findMyBook = myBookRepository.findByIdAndMemberId(bookId, memberId)
            .orElseThrow(() -> new BookNotFoundException(
                String.format(BOOK_NOT_FOUND, memberId, bookId)));

        Member findMember = memberRepository.findById(memberId)
            .orElseThrow(
                () -> new MemberNotFoundException(String.format(USER_NOT_FOUND, memberId)));

        // 직접 엔티티의 값을 변경
        // 더티 체킹으로 자동 업데이트
        findMyBook.updateStatus(requestDTO);

        // 업데이트 할때 마다 달력 스탬프에 기록 (읽고 있는중, 다 읽은 책 만)
        if (BookStatus.READING.name().equals(requestDTO.getStatus()) || BookStatus.COMPLETED.name()
            .equals(requestDTO.getStatus())) {
            recordCalendarStamp(findMember, findMyBook);
        }
        
        return findMyBook.getId();
    }

    @Override
    public ScrollResponseDTO<MyBookResponseDTO> getMyBookList(Long memberId,
        ScrollRequestDTO requestDTO) {

        // 해당 status에 따른 전체 기록된 책 수
        Long totalElements = myBookRepository.countByMemberIdAndStatus(memberId, requestDTO);

        // findBooksWithScroll안에서 size + 1개 데이터 조회해서 가져옴 (size가 10이면 11개 가져옴)
        List<MyBook> myBooks = myBookRepository.findBooksWithScroll(memberId, requestDTO);

        boolean hasNext = false;
        if (myBooks.size() > requestDTO.getSize()) { // 11 > 10 이면 있다는 뜻
            hasNext = true;
            myBooks.remove(myBooks.size() - 1); // 11개 가져온 걸 10개를 보내기 위해
        }

        List<MyBookResponseDTO> dtoList = myBooks.stream()
            .map(myBook -> {
                Integer count = calendarStampRepository.countByMemberIdAndMyBookId(memberId,
                    myBook.getId());
                return MyBookResponseDTO.from(myBook, count);
            })
            .collect(Collectors.toList());

        // 마지막 항목의 ID 설정
        Long lastId = myBooks.isEmpty() ?
            requestDTO.getLastId() :    // 조회된 목록이 비어있는 경우를 대비해 삼항 연산자 사용
            myBooks.get(myBooks.size() - 1).getId(); // lastId를 요청 DTO의 값이 아닌, 실제 조회된 마지막 항목의 ID로 설정

        return ScrollResponseDTO.<MyBookResponseDTO>withAll()
            .content(dtoList)
            .hasNext(hasNext)
            .lastId(lastId) // 조회된 목록의 마지막 항목의 ID
            .numberOfElements(dtoList.size())
            .totalElements(totalElements)
            .build();
    }

    @Override
    public MyBookResponseDTO getMyBook(Long memberId, Long bookId) {

        MyBook findBook = myBookRepository.findByIdAndMemberIdWithAladinBook(bookId, memberId)
            .orElseThrow(
                () -> new BookNotFoundException(String.format(BOOK_NOT_FOUND, memberId, bookId)));

        Integer count = calendarStampRepository.countByMemberIdAndMyBookId(memberId,
            findBook.getId());

        return MyBookResponseDTO.from(findBook, count);
    }

    @Override
    public void deleteMyBook(Long memberId, Long bookId) {
        // 1. 삭제 전에 책이 존재하는지 확인
        MyBook myBook = myBookRepository.findByIdAndMemberId(bookId, memberId)
                .orElseThrow(() -> new BookNotFoundException(
                        String.format(BOOK_NOT_FOUND, memberId, bookId)
                ));
//
//        List<Discussion> check = discussionRepository.findByMemberIdAndBookId(memberId, bookId);
//
//        // TODO : 딜리트를 하자니 myBook으로 토론 한번 생성하면 지울 방법이 현재 없는듯, 토론은 소프트 딜리트여서
//        // 아니면 myBook도 소프트 딜리트해서 안보이게 하고 지웠다 같은 걸 만들면 소프트 딜리트를 풀고 새로운 정보 처리??
//        if (!check.isEmpty()) {
//            throw new DiscussionException("MyBook을 삭제하기 위해선 해당 기록으로 만들어진 Discussion이 없어야 합니다. 관리자에게 문의하세요");
//        }

        // 2. 연관된 CalendarStamp 먼저 삭제, 안하면 연결 되어있기에 myBook 삭제가 안됨
        calendarStampRepository.deleteAllByMyBookId(bookId);

        // 3. 존재하면 삭제 실행 - 소프트 딜리트로 변경
        myBook.updateDeleted(true);
        myBook.updateDeletedAt(LocalDateTime.now());
    }

    @Override
    public ScrollResponseDTO<MyBookResponseDTO> getOtherMemberBookList(Long loginMemberId,
        Long targetMemberId,
        ScrollRequestDTO requestDTO) {

        // 해당 status에 따른 전체 기록된 책 수
        Long totalElements = myBookRepository.countByMemberIdAndStatus(targetMemberId, requestDTO);

        // findBooksWithScroll안에서 size + 1개 데이터 조회해서 가져옴 (size가 10이면 11개 가져옴)
        List<MyBook> otherMemberBooks = myBookRepository.findBooksWithScroll(targetMemberId,
            requestDTO);

        boolean hasNext = false;
        if (otherMemberBooks.size() > requestDTO.getSize()) { // 11 > 10 이면 있다는 뜻
            hasNext = true;
            otherMemberBooks.remove(otherMemberBooks.size() - 1); // 11개 가져온 걸 10개를 보내기 위해
        }

        Set<MyBook> loginMemberHavingBooks = myBookRepository.findByMemberId(loginMemberId);

        List<MyBookResponseDTO> dtoList = otherMemberBooks.stream()
            .map(otherMemberBook -> {
                Integer count = calendarStampRepository.countByMemberIdAndMyBookId(targetMemberId,
                    otherMemberBook.getId());

                MyBook loginMemberBook = loginMemberHavingBooks.stream()
                    .filter(book -> book.getIsbn13().equals(otherMemberBook.getIsbn13()))
                    .findFirst()
                    .orElse(null);
                if (loginMemberBook != null) {
                    return MyBookResponseDTO.from(otherMemberBook, count,
                        loginMemberBook.getStatus().name());
                }
                return MyBookResponseDTO.from(otherMemberBook, count);
            })
            .collect(Collectors.toList());

        // 마지막 항목의 ID 설정
        Long lastId = otherMemberBooks.isEmpty() ?
            requestDTO.getLastId() :    // 조회된 목록이 비어있는 경우를 대비해 삼항 연산자 사용
            otherMemberBooks.get(otherMemberBooks.size() - 1)
                .getId(); // lastId를 요청 DTO의 값이 아닌, 실제 조회된 마지막 항목의 ID로 설정

        return ScrollResponseDTO.<MyBookResponseDTO>withAll()
            .content(dtoList)
            .hasNext(hasNext)
            .lastId(lastId) // 조회된 목록의 마지막 항목의 ID
            .numberOfElements(dtoList.size())
            .totalElements(totalElements)
            .build();
    }

    @Override
    public MyBookResponseDTO getOtherMemberBook(Long loginMemberId, Long targetMemberId,
        Long myBookId) {
        MyBook findBook = myBookRepository.findByIdAndMemberIdWithAladinBook(myBookId,
                targetMemberId)
            .orElseThrow(() -> new BookNotFoundException(
                String.format(BOOK_NOT_FOUND, targetMemberId, myBookId)));

        Integer stampCount = calendarStampRepository.countByMemberIdAndMyBookId(targetMemberId,
            findBook.getId());

        Optional<MyBook> loginMemberMyBook = myBookRepository.findByMemberIdAndIsbn13(loginMemberId,
            findBook.getIsbn13());

        if (loginMemberMyBook.isPresent()) {
            if (loginMemberMyBook.get().getIsbn13().equals(findBook.getIsbn13())) {
                return MyBookResponseDTO.from(findBook, stampCount,
                    loginMemberMyBook.get().getStatus().name());
            }
        }
        return MyBookResponseDTO.from(findBook, stampCount);
    }

    @Override
    public void insertNewBookByWish(Long memberId, Long targetMyBookId) {

        MyBook findTargetMyBook = myBookRepository.findById(targetMyBookId)
            .orElseThrow(() -> new BookNotFoundException(
                String.format(BOOK_NOT_FOUND_BY_ID, targetMyBookId)));

        AladinBook findAladinBook = findTargetMyBook.getAladinBook();

        if (findAladinBook == null) {
            throw new AladinBookNotFoundException(String.format(ALADIN_BOOK_NOT_FOUND));
        }

        Member findLoginMember = memberRepository.findById(memberId)
            .orElseThrow(() -> new MemberException(String.format(MEMBER_NOT_FOUND, memberId)));

        Optional<MyBook> findRecordMyBook = myBookRepository.findByMemberIdAndIsbn13(memberId,
            findTargetMyBook.getIsbn13());

        if (findRecordMyBook.isPresent()) {
            throw new BookDuplicateNotAllowException(BOOK_CAN_NOT_DUPLICATE);
        }

        MyBook myBook = MyBook.builder()
            .member(findLoginMember)
            .aladinBook(findAladinBook)
            .isbn13(findAladinBook.getIsbn13())
            .status(BookStatus.WISH)
            .build();

        myBookRepository.save(myBook);
    }

    private Long saveBookAndCalenarStampByStatus(
        BookStatusRequestDTO requestDTO, MyBook myBook,
        AladinBook findAladinBook, Member findMember) {
        
        MyBook savedMyBook = null;
        // 업데이트 할때 마다 달력 스탬프에 기록 (읽고 있는중, 다 읽은 책 만)
        if (BookStatus.COMPLETED.name().equals(requestDTO.getStatus())) {
            savedMyBook = saveBookWithCompletedStatus(requestDTO, myBook, findAladinBook);
            recordCalendarStamp(findMember, savedMyBook);
        } else if (BookStatus.READING.name().equals(requestDTO.getStatus())) {
            savedMyBook = saveBookWithReadingStatus(requestDTO, myBook);
            recordCalendarStamp(findMember, savedMyBook);
        } else if (BookStatus.WISH.name().equals(requestDTO.getStatus())) {
            savedMyBook = saveBookWithWishStatus(myBook);
        } else if (BookStatus.STOPPED.name().equals(requestDTO.getStatus())) {
            savedMyBook = saveBookWithStoppedStatus(requestDTO, myBook);
        } else {
            throw new InvalidStatusException(String.format(INVALID_STATUS, requestDTO.getStatus()));
        }
        
        return savedMyBook.getId();
    }

    private MyBook saveBookWithStoppedStatus(BookStatusRequestDTO requestDTO, MyBook myBook) {
        myBook = myBook.toBuilder()
            .status(BookStatus.STOPPED)
            .myRating(requestDTO.getMyRating())
            .oneLineReview(requestDTO.getOneLineReview())
            .currentPage(requestDTO.getCurrentPage())
            .startDate(requestDTO.getStartDate())
            .endDate(requestDTO.getEndDate())
            .updateCount(requestDTO.getUpdateCount())
            .build();

        return myBookRepository.save(myBook);
    }

    private MyBook saveBookWithWishStatus(MyBook myBook) {
        myBook = myBook.toBuilder()
            .status(BookStatus.WISH)
            .build();

        return myBookRepository.save(myBook);
    }

    private MyBook saveBookWithReadingStatus(BookStatusRequestDTO requestDTO, MyBook myBook) {
        myBook = myBook.toBuilder()
            .status(BookStatus.READING)
            .myRating(requestDTO.getMyRating())
            .currentPage(requestDTO.getCurrentPage())
            .startDate(LocalDate.now())
            .updateCount(requestDTO.getUpdateCount())
            .build();

        return myBookRepository.save(myBook);
    }

    private MyBook saveBookWithCompletedStatus(BookStatusRequestDTO requestDTO, MyBook myBook,
        AladinBook findAladinBook) {
        myBook = myBook.toBuilder()
            .status(BookStatus.COMPLETED)
            .myRating(requestDTO.getMyRating())
            .oneLineReview(requestDTO.getOneLineReview())
            .currentPage(findAladinBook.getItemPage()) // 다 읽었으니 100%로 만들기 위해
            .startDate(requestDTO.getStartDate())
            .endDate(requestDTO.getEndDate())
            .updateCount(requestDTO.getUpdateCount())
            .build();

        return myBookRepository.save(myBook);
    }

    private void recordCalendarStamp(Member findMember, MyBook myBook) {
        try {

            // 현재 책의 readDateCount를 올리기 위해서
            Integer count = calendarStampRepository.countByMemberIdAndMyBookId(findMember.getId(),
                myBook.getId());

            CalendarStamp calendarStamp = CalendarStamp.builder()
                .member(findMember)
                .myBookId(myBook.getId())
                .bookTitle(myBook.getAladinBook().getTitle())
                .bookAuthor(myBook.getAladinBook().getAuthor())
                .bookCover(myBook.getAladinBook().getCover())
                .recordedAt(LocalDate.now())
                .status(myBook.getStatus())
                .itemPage(myBook.getAladinBook().getItemPage())
                .currentPage(myBook.getCurrentPage())
                .startDate(myBook.getStartDate())
                .endDate(myBook.getEndDate())
                .readDateCount(count)
                .myRating(myBook.getMyRating())
                .oneLineReview(myBook.getOneLineReview())
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
