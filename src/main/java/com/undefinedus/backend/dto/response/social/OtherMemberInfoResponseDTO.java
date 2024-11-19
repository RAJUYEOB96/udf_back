package com.undefinedus.backend.dto.response.social;

import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.enums.PreferencesType;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OtherMemberInfoResponseDTO {
    
    private Long id; // 해당 멤버의 member id
    
    private String nickname;
    
    private String profileImage; // 프로필 이미지 URL
    
    private Set<PreferencesType> preferences; // 해당 멤버의 취향들
    
    private boolean isFollowing; // 내가 그 멤버를 팔로우했는지
    
    public static OtherMemberInfoResponseDTO from(Member otherMember, Set<Long> followingIds) {
        return OtherMemberInfoResponseDTO.builder()
               .id(otherMember.getId())
               .nickname(otherMember.getNickname())
               .profileImage(otherMember.getProfileImage())
               .preferences(otherMember.getPreferences())
                .isFollowing(followingIds.contains(otherMember.getId()) ? true : false)
               .build();
    }
}
