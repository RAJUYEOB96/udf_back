package com.undefinedus.backend.dto.response.social;

import com.undefinedus.backend.domain.entity.Member;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberSocialInfoResponseDTO {
    
    private Long id; // 해당 member의 id
    
    private String profileImage; // 프로필 이미지 URL? 이름?
    
    private String nickname;
    
    private int followingCount; //  내가 팔로우하는 관계들 (내가 따라가는)
    
    private int followerCount; // 나를 팔로우하는 관계들 (나를 따라오는)
    
    // 내가 다른 member를 볼때 내가 그사람을 팔로우 했는지 안했는지
    // 내가 내것을 볼때는 null 예정
    private Boolean isFollowing; // 내가 그 사람을 팔로우 했는지 안했는지
    
    public static MemberSocialInfoResponseDTO from(Member member, Integer followingCount, Integer followerCount) {
        return from(member, followingCount, followerCount, null);
    }
    
    public static MemberSocialInfoResponseDTO from(Member member, Integer followingCount, Integer followerCount,
            Boolean isFollowing) {
        return MemberSocialInfoResponseDTO.builder()
                .id(member.getId())
                .profileImage(member.getProfileImage())
                .nickname(member.getNickname())
                .followingCount(followingCount)
                .followerCount(followerCount)
                .isFollowing(isFollowing)
                .build();
    }
    
    // 내가 남의 socialInfo를 볼때 넣어주기 위한
    public void updateIsFollowing(boolean isFollowing) {
        this.isFollowing = isFollowing;
    }
}
