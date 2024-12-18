package com.undefinedus.backend.dto.request.discussionComment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscussionCommentsScrollRequestDTO {

    @Builder.Default
    private Long lastId = 0L; // 마지막으로 로드된 항목의 ID
    
    @Builder.Default
    private int size = 100;    // 한 번에 로드할 항목 수
}
