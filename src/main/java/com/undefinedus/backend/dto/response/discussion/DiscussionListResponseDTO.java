package com.undefinedus.backend.dto.response.discussion;

import com.undefinedus.backend.domain.enums.ViewStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscussionListResponseDTO {

    // 상세 페이지를 위한 필요 정보 //
    private Long discussionId;
    private String isbn13;

    private String bookTitle;
    
    private Long memberId; // 작성자 Id

    private String memberName;  // 작성자

    private String title;    // 토론 제목

    private Long agree; // 토론 찬성 반대 참여자 수를 세기 위해 필요 예(찬성 2)

    private Long disagree; // 토론 찬성 반대 참여자 수를 세기 위해 필요 예(반대 2)

    private LocalDateTime createdDate; // BaseEntity 에서 뽑아써야 함

    private LocalDateTime startDateTime; // 토론 시작 시간

    private LocalDateTime closedAt;  // 토론 끝나는 시간

    private Long views; // 조회 수

    private Long commentCount; // 댓글 수

    private Integer agreePercent; // AI가 분석한 결과 찬성

    private Integer disagreePercent; // AI가 분석한 결과 반대

    private String cover; // discussion 에서 myBook에 있는 isbn13을 뽑아 cover 링크를 뽑아 저장

    private String status; // 게시물 상태
    
    private ViewStatus viewStatus; // Blocked 되었는지에 대한 필드

    private Integer agreeCommentCount;

    private Integer disagreeCommentCount;
}
