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
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private MyBook myBook;
    
    // 달력에 표시할 책 표지 URL // 이건 book 에서 get으로 표지이미지를 들고 오면 되는거 아닌가?
    @Column(length = 255, nullable = false)
    private String bookCoverUrl;
    
    @Column(nullable = false)
    private LocalDate recordedAt;     // 독서 기록 날짜
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BookStatus status;
    
}
