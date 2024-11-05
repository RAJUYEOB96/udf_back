package com.undefinedus.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReadingRecord {    // 다 읽은책, 읽고 있는책, 중단한 책은 생성이든, 수정이든 이부분도 함께 저장해야함
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // 기록과 연결된 책 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    // 독서 기록을 남긴 회원 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    // 달력에 표시할 책 표지 URL // 이건 book 에서 get으로 표지이미지를 들고 오면 되는거 아닌가?
    @Column(length = 255, nullable = false)
    private String bookCoverUrl;
    
    // 독서 기록 날짜
    @Column(nullable = false)
    private LocalDateTime recordedAt;
}
