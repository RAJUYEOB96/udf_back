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
import jakarta.persistence.OneToOne;
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
    
    // === 사용자의 독서 활동 정보 === //
    @Column                      // 아직 안읽었을 수 있기 때문에 null 가능
    private Double myRating;    // 다 읽은 책, 읽고 있는 책 일때 사용할 별점
    
    @Column(length = 255)
    private String oneLineReview; // 한줄평
    
    @Column                     // 아직 안읽었을 수 있기 때문에 null 가능
    private Integer currentPage;   // readPageNumber -> currentPage (더 직관적)
    
    // startDate, finishDate는 총 책을 읽기 시작해서 끝난 날짜
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


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aladin_book_id")
    private AladinBook aladinBook;

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public void setMyRating(Double myRating) {
        this.myRating = myRating;
    }

    public void setOneLineReview(String oneLineReview) {
        this.oneLineReview = oneLineReview;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setFinishDate(LocalDate finishDate) {
        this.finishDate = finishDate;
    }

    public void setReadDates(List<LocalDate> readDates) {
        this.readDates = readDates;
    }

}
