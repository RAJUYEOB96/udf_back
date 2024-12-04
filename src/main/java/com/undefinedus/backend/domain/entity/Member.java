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
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
@ToString(exclude = {"socialLogin", "followings", "followers"})
public class Member extends BaseEntity {
    
    // === ID === //
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // === 기본 정보 === //
    @Column(length = 40, unique = true, nullable = false)
    private String username;    // 아이디 (일반 로그인 유저 : 이메일형식, 소셜 로그인 유저 : 숫자형식(kakaoId))
    
    @Column(length = 100, nullable = false) // 암호화해서 길이 늘어남
    private String password;   // 비밀번호
    
    @Column(length = 10, unique = true, nullable = false)
    @Size(min = 2, max = 10)
    private String nickname;    // 일반, 소셜 둘다 회원 가입시 임의 작성
    
    // === 프로필 정보 === //
    @Column(length = 255)
    private String profileImage;    // 프로필 이미지 URL
    
    @Column(length = 500)
    private String introduction;    // 자기소개
    
    @Column
    private LocalDate birth;
    
    @Column
    private String gender;
    
    // === 소셜 로그인 === //
    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private SocialLogin socialLogin;    // null 인 경우 일반 로그인
    
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
    @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Follow> followings = new HashSet<>(); // 내가 팔로우하는 관계들 (내가 따라가는)
    
    @OneToMany(mappedBy = "following", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private Set<Follow> followers = new HashSet<>(); // 나를 팔로우하는 관계들 (나를 따라오는)
    
    // === 설정 정보 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isPublic = true;    // 소셜 계정 공개 여부 (true: 공개, false: 비공개)
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isMessageToKakao = false;    // 책갈피 내용 카톡으로 보낼지 말지

    @Column(nullable = false)
    @Builder.Default
    private String honorific = "초보리더"; // 칭호 // version.1 에서는 기본 칭호를 유지할 예정

    // === 메시지 전송을 위한 token === //
    @Column
    private String kakaoRefreshToken;

    // 알림 관련은 한달안에 알림 기능까지 넣기는 빡세다고 생각 다음 버전 만들시 추가 예정
    
    // === Soft Delete 관련 === //
    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false; // softDelete
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // === 메서드 === //
    public void addRole(MemberType memberType) {
        memberRoleList.add(memberType);
    }
    
    public void clearRole() {
        memberRoleList.clear();
    }
    
    public void setSocialLogin(SocialLogin socialLogin) {
        this.socialLogin = socialLogin;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setNickname(
        @Size(min = 2, max = 10) String nickname) {
        this.nickname = nickname;
    }

    public void updateKakaoRefreshToken(String kakaoRefreshToken) {
        this.kakaoRefreshToken = kakaoRefreshToken;
    }
}
