package com.undefinedus.backend.domain.entity;

import com.undefinedus.backend.domain.enums.BookStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE book SET is_deleted = true, deleted_at = NOW() WHERE id = ?")  // soft delete 쿼리 설정
// @SQLRestriction은 Spring Data JPA Repository 메서드 사용시에만 자동 적용
// JPQL이나 QueryDSL 사용시에는 조건을 직접 추가해야 함
@SQLRestriction("is_deleted = false")  // @Where 대신 @SQLRestriction 사용
public class Book extends BaseEntity {
    
    // === ID & 연관관계 매핑 === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;  // 일단 단방향 설정 해놓았음 필요시 Member 테이블에 추가해서 양방향 만들기
    
    // === 책 상태 정보 === //
    @Column(nullable = false)  // name = "bookStatus" 는 불필요 (자동으로 book_status로 변환됨)
    @Enumerated(EnumType.STRING)
    private BookStatus status;  // bookStatus -> status (Book 엔티티 안에 있으므로 book 접두어 불필요)
    
    // === 책 기본 정보 (API에서 가져오는 정보) === //
    @Column(length = 50, nullable = false)
    private String title;   // 책 제목
    
    @Column(length = 100, nullable = false)
    private String author; // 저자
    
    @Column(length = 200, nullable = false)
    private String coverImage; // 표지 이미지
    
    @Column(length = 13, nullable = false) // api로 불러올 거기 때문에 필수로 값이 들어가야함
    private String isbn;    // ISBN-13 형식
    
    @Column(length = 500, nullable = false)
    private String summary; // 요약
    
    @Column(length = 100, nullable = false)
    private String publisher; // 출판사
    
    @Column(length = 50, nullable = false)
    private String category;        // 책 카테고리/장르 // // Member의 preferences와 일치하는 것을 책추천할때 해줌
    
    @Column(nullable = false)   // api로 불러올 거기 때문에 필수로 값이 들어가야함
    private Double averageRating; // api를 사용해서 정보를 들고 옴 (읽고 싶은 책 에 필요)
    
    @Column(nullable = false) // api로 불러올 거기 때문에 필수로 값이 들어가야함
    private Integer totalPages;    // totalPageNumber -> totalPages (더 간단하고 명확)
    
    // === 사용자의 독서 활동 정보 === //
    @Column                      // 아직 안읽었을 수 있기 때문에 null 가능
    private Double myRating;    // 다 읽은 책, 읽고 있는 책 일때 사용할 별점
    
    @Column(length = 255)
    private String oneLineReview; // 한줄평
    
    @Column                     // 아직 안읽었을 수 있기 때문에 null 가능
    private Integer currentPage;   // readPageNumber -> currentPage (더 직관적)
    
    // startDate, endDate는 총 책을 읽기 시작해서 끝난 날짜
    @Column
    private LocalDate startDate;    // 읽기 시작한 날짜
    @Column
    private LocalDate finishDate;   // 완독한 날짜
    
 
    @ElementCollection(targetClass = LocalDate.class, fetch = FetchType.LAZY)
    @CollectionTable(
            name = "read_dates",    // real_readed_date -> read_dates (더 간단)  // 테이블 이름 명시
            joinColumns = @JoinColumn(name = "book_id")
    )
    @Column(name = "read_date")     // 컬럼명 지정
    @Builder.Default
    private List<LocalDate> readDates = new ArrayList<>(); // 이건 실직적으로 읽고 기록한 날짜
    
    // === Soft Delete 관련 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false; // softDelete
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
}
