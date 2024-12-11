package com.undefinedus.backend.dto.response.discussion;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscussionDetailResponseDTO {


    private Long discussionId;  // 신고 용

    private String bookTitle;  // 어떤 책의 토론인지

    private String memberName;  // 작성자

    private String title;    // 토론 제목

    private String content;

    private Long agree; // 토론 찬성 반대 참여자 수를 세기 위해 필요 예(찬성 2)

    private Long disagree; // 토론 찬성 반대 참여자 수를 세기 위해 필요 예(반대 2)

    private LocalDateTime startDate; // 토론을 시작할 시간 // 토론 시작 시간은 createdDate보다 최소 24시간 뒤 최대 7일 이여야 한다.

    private LocalDateTime closedAt; // 토론 끝나는 시간

    private LocalDateTime createdDate; // BaseEntity 에서 뽑아써야 함

    private Long views; // 조회수

    private Long commentCount; // 댓글 수

    private String cover; // discussion 에서 myBook에 있는 isbn13을 뽑아 cover 링크를 뽑아 저장

    private String status; // 게시물 상태

    private Integer agreePercent; // AI가 분석한 결과 찬성

    private Integer disagreePercent; // AI가 분석한 결과 반대
    
    private Boolean isReport;   // 로그인 사용자가 볼때 신고 했었는지
    
    private String isAgree; // isAgree, disAgree, null

}
