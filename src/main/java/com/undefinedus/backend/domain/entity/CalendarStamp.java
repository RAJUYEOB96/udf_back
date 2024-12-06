package com.undefinedus.backend.domain.entity;


import com.undefinedus.backend.domain.enums.BookStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CalendarStamp {    // 달력 화면에서만 쓸 예정이지만 기록이 등록, 수정 될때마다 이것도 넣어줘야함

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 변경된 부분: MyBook의 ID만 참조
    @Column(nullable = false)
    private Long myBookId;

    // 당시의 책 정보를 직접 저장
    @Column(length = 255, nullable = false)
    private String bookTitle;        // 책 제목

    @Column(length = 200, nullable = false)  // 여러 저자가 있을 수 있음 (김재원, 김용)
    private String bookAuthor;        // 저자

    // 달력에 표시할 책 표지 URL
    @Column(length = 255, nullable = false)
    private String bookCover;

    @Column(nullable = false)
    private LocalDate recordedAt;     // 독서 기록 날짜

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookStatus status;

    @Column
    private Integer itemPage;         // 총 페이지 수

    @Column                     // 아직 안읽었을 수 있기 때문에 null 가능
    private Integer currentPage;   // readPageNumber -> currentPage (더 직관적)

    @Column
    private LocalDate startDate;    // 읽기 시작한 날짜

    @Column
    private LocalDate endDate;   // 완독한/멈춘 날짜

    @Column
    private Integer readDateCount;  // readCount 이건 따로 계산해서 넣어줄 예정

    @Column
    private Double myRating;  // 다 읽은 책, 읽고 있는 책 일때 사용할 별점

}
