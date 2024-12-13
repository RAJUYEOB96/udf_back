package com.undefinedus.backend.dto.request.discussionComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscussionScrollRequestDTO {

    @Builder.Default
    private Long lastId = 0L; // 마지막으로 로드된 항목의 ID
    
    @Builder.Default
    private int size = 10;    // 한 번에 로드할 항목 수
    
    @Builder.Default
    private String sort = "desc"; // asc : 오름차순, desc : 내림차순
    
    private String search;    // 검색어
    
    @Builder.Default
    private String status = "PROPOSED";   // 탭 구분 (예: PROPOSED, SCHEDULED, IN_PROGRESS, ANALYZING, COMPLETED, BLOCKED)
}
