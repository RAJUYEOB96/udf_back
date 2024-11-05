package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Book;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.dto.request.book.BookChoiceRequestDTO;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.BookRepository;
import com.undefinedus.backend.repository.MemberRepository;
import jakarta.transaction.Transactional;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Log4j2
@Transactional
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final AladinBookRepository aladinBookRepository;

    private final MemberRepository memberRepository;

    @Override
    public Map<String, Long> choiceBook(BookChoiceRequestDTO bookChoiceRequestDTO) {

        Optional<AladinBook> savedAladinBook = aladinBookRepository.findByIsbn13(
            bookChoiceRequestDTO.getAladinBookRequestDTO().getIsbn13());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        Member member = memberRepository.findByUsername(name).orElseThrow();

        System.out.println("member = " + member);

        // DB에 저장되어 있고 내 책장에도 책이 저장되어 있는 것 -> 내 책장의 알라딘 ID
        Optional<Book> memberBook = bookRepository.findMemberBook(member.getId());

        Long bookId = 0L;

        // DB에 저장된 책 없음
        if (savedAladinBook.isEmpty()) {

            // 검색한 책이 DB에 없으면, 책을 DB에 저장하고, Book 스키마(내 책장)에 내 user_id와 book_id를 저장한다.
            bookId = saveSelectedAladinBook(bookChoiceRequestDTO, member);

            return Map.of("result", bookId);
            // DB에 저장된 책 있음
        } else {

            // 책장에 책이 없으면
            if (memberBook.isEmpty()) {

                // 검색한 책이 DB에 있지만, 내 Book 스키마(내 책장)에 없으면, DB에는 저장안하고, Book 스키마(내 책장)에 내 user_id와 book_id를 저장한다.
                bookId = saveSelectedBook(bookChoiceRequestDTO,
                    member);

                return Map.of("result", bookId);

                // 책장에 있는 책의 알라딘 ID와 알라딘 ID가 같으면
            } else if (Objects.equals(memberBook.get().getAladinBook().getId(),
                savedAladinBook.get().getId())) {

                // 검색한 책이 DB에 있고, 내 Book 스키마(내 책장)에도 있으면, 저장이 안된다.
                return Map.of("result", -1L);
            }
        }


        Book myBook = bookRepository.findById(bookId).orElseThrow();

        myBook.setStatus(
            BookStatus.valueOf(bookChoiceRequestDTO.getBookStatusRequestDTO().getStatus()));

        BookStatus bookStatus = BookStatus.valueOf(
            bookChoiceRequestDTO.getBookStatusRequestDTO().getStatus());

        if (bookStatus.equals(BookStatus.COMPLETED)) {

            myBook.setMyRating(bookChoiceRequestDTO.getBookStatusRequestDTO().getMyRating());
            myBook.setOneLineReview(
                bookChoiceRequestDTO.getBookStatusRequestDTO().getOneLineReview());
            myBook.setFinishDate(bookChoiceRequestDTO.getBookStatusRequestDTO().getFinishDate());
            myBook.setCurrentPage(bookChoiceRequestDTO.getBookStatusRequestDTO().getCurrentPage());

            bookRepository.save(myBook);

        } else if (bookStatus.equals(BookStatus.STOPPED)) {

            myBook.setMyRating(bookChoiceRequestDTO.getBookStatusRequestDTO().getMyRating());

            bookRepository.save(myBook);

        } else if (bookStatus.equals(BookStatus.READING)) {

            myBook.setStartDate(bookChoiceRequestDTO.getBookStatusRequestDTO().getStartDate());
            myBook.setCurrentPage(bookChoiceRequestDTO.getBookStatusRequestDTO().getCurrentPage());
            myBook.setReadDates(bookChoiceRequestDTO.getBookStatusRequestDTO().getReadDates());

            bookRepository.save(myBook);

        } else if (bookStatus.equals(BookStatus.WISH)) {

            bookRepository.save(myBook);

        }

        return null;

    }

    private Long saveSelectedAladinBook(BookChoiceRequestDTO bookChoiceRequestDTO, Member member) {

        AladinBook aladinBook = AladinBook.builder()
            .isbn13(bookChoiceRequestDTO.getAladinBookRequestDTO().getIsbn13())
            .title(bookChoiceRequestDTO.getAladinBookRequestDTO().getTitle())
            .subTitle(bookChoiceRequestDTO.getAladinBookRequestDTO().getSubTitle())
            .author(bookChoiceRequestDTO.getAladinBookRequestDTO().getAuthor())
            .summary(bookChoiceRequestDTO.getAladinBookRequestDTO().getSummary())
            .link(bookChoiceRequestDTO.getAladinBookRequestDTO().getLink())
            .cover(bookChoiceRequestDTO.getAladinBookRequestDTO().getCover())
            .description(bookChoiceRequestDTO.getAladinBookRequestDTO().getDescription())
            .publisher(bookChoiceRequestDTO.getAladinBookRequestDTO().getPublisher())
            .category(bookChoiceRequestDTO.getAladinBookRequestDTO().getCategory())
            .customerReviewRank(
                bookChoiceRequestDTO.getAladinBookRequestDTO().getCustomerReviewRank())
            .isAdult(bookChoiceRequestDTO.getAladinBookRequestDTO().isAdult())
            .pagesCount(bookChoiceRequestDTO.getAladinBookRequestDTO().getPagesCount())
            .build();


        AladinBook newBook = aladinBookRepository.save(aladinBook);

        Book choicedBook = Book.builder()
            .member(member)
            .status(BookStatus.valueOf(bookChoiceRequestDTO.getBookStatusRequestDTO().getStatus()))
            .aladinBook(newBook)
            .build();

        Book book = bookRepository.save(choicedBook);

        return book.getId();
    }

    private Long saveSelectedBook(BookChoiceRequestDTO bookChoiceRequestDTO, Member member) {

        Optional<AladinBook> byIsbn13 = aladinBookRepository.findByIsbn13(
            bookChoiceRequestDTO.getAladinBookRequestDTO().getIsbn13());

        Book choicedBook = Book.builder()
            .member(member)
            .status(BookStatus.valueOf(bookChoiceRequestDTO.getBookStatusRequestDTO().getStatus()))
            .aladinBook(byIsbn13.get())
            .build();

        Book book = bookRepository.save(choicedBook);

        return book.getId();
    }
}
