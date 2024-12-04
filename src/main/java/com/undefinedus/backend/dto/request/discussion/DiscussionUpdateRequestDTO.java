package com.undefinedus.backend.dto.request.discussion;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscussionUpdateRequestDTO {

    private String title;    // 토론 제목

    private String content;     // 토론 주제 글

    private LocalDateTime modifyStartTime;      // 수정한 토론 시작 시간

}
