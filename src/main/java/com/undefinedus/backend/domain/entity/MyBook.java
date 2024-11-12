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
@Builder(toBuilder = true)
@ToString
public class MyBook extends BaseEntity {
    
    // === ID & 연관관계 매핑 === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // 일단 단방향 설정 해놓았음 필요시 Member 테이블에 추가해서 양방향 만들기
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aladin_book_id")
    private AladinBook aladinBook;
    
    // === 본인이 책을 추가할 때 체크를 위한 필드 === //
    @Column(length = 13)
    private String isbn13;
    
    // === 책 상태 정보 === //
    @Column(nullable = false)  // name = "bookStatus" 는 불필요 (자동으로 book_status로 변환됨)
    @Enumerated(EnumType.STRING)
    private BookStatus status;  // bookStatus -> status (Book 엔티티 안에 있으므로 book 접두어 불필요)
    
    // === 사용자의 독서 활동 정보 === //
    @Column                      // 아직 안읽었을 수 있기 때문에 null 가능
    private Double myRating;    // 다 읽은 책, 읽고 있는 책 일때 사용할 별점
    
    @Column(length = 100)
    private String oneLineReview; // 한줄평
    
    @Column                     // 아직 안읽었을 수 있기 때문에 null 가능
    private Integer currentPage;   // readPageNumber -> currentPage (더 직관적)
    
    @Column
    private Integer updateCount; // 리액트에서 넘어오는 값을 저장만 하기로 합의
    
    // startDate, endDate는 총 책을 읽기 시작해서 끝난 날짜
    @Column
    private LocalDate startDate;    // 읽기 시작한 날짜
    @Column
    private LocalDate endDate;   // 완독한/멈춘 날짜
    
}
