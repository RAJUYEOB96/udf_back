package com.undefinedus.backend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
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
public class FriendBookPhrase {
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 내용 === //
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // 구절을 책갈피한 사용자 (친구 구절을 저장한 회원)
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_phrase_id", nullable = false)
    private BookPhrase bookPhrase; // 친구의 구절
    
    @Column(nullable = false)
    @Builder.Default
    private LocalDate bookmarkedAt = LocalDate.now(); // 책갈피한 날짜
    
}
