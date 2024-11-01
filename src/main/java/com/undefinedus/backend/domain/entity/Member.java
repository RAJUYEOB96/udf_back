package com.undefinedus.backend.domain.entity;

import com.undefinedus.backend.domain.enums.MemberType;
import com.undefinedus.backend.domain.enums.PreferencesType;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@SQLDelete(sql = "UPDATE member SET is_deleted = true, deleted_at = NOW() WHERE id = ?")  // soft delete 쿼리 설정
// @SQLRestriction은 Spring Data JPA Repository 메서드 사용시에만 자동 적용
// JPQL이나 QueryDSL 사용시에는 조건을 직접 추가해야 함:
@SQLRestriction("is_deleted = false")  // @Where 대신 @SQLRestriction 사용
public class Member extends BaseEntity {
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 기본 정보 === //
    @Column(length = 40, unique = true, nullable = false)
    private String username;    // 아이디 (일반 로그인 유저 : 이메일형식, 소셜 로그인 유저 : 숫자?형식)
    
    @Column(length = 70, nullable = false)
    private String password;   // 비밀번호
    
    @Column(length = 30, unique = true, nullable = false)
    private String nickname;
    
    // === 프로필 정보 === //
    @Column(length = 255)
    private String profileImage;    // 프로필 이미지 URL
    
    @Column(length = 500)
    private String introduction;    // 자기소개
    
    // === 소셜 로그인 === //
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private SocialLogin socialLogin;
    
    // === 권한 및 취향 정보 === //
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "member_roles",  // 테이블 이름 명시
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<MemberType> memberRoleList = new ArrayList<>();
    
    @ElementCollection(targetClass = PreferencesType.class, fetch = FetchType.LAZY)
    @CollectionTable(
            name = "member_preferences",  // 테이블 이름 명시
            joinColumns = @JoinColumn(name = "member_id")
    )
    @Column(name = "preference", nullable = false)  // 컬럼 이름 명시
    @Enumerated(EnumType.STRING)
    @Builder.Default  // preferences도 초기화하면 좋음
    private Set<PreferencesType> preferences = new HashSet<>(); // Book의 category와 일치하는 것을 책추천할때 해줌
    
    // === 팔로우 관계 === //
    @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Follow> followings = new HashSet<>(); // 내가 팔로우하는 관계들
    
    @OneToMany(mappedBy = "following", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Follow> followers = new HashSet<>(); // 나를 팔로우하는 관계들
    
    // === 설정 정보 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isPublic = true;    // 소셜 계정 공개 여부 (true: 공개, false: 비공개)
    
    // === Soft Delete 관련 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false; // softDelete
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    
    
}
