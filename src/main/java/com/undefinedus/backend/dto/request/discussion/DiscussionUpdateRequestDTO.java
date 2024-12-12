package com.undefinedus.backend.dto.request.discussion;

import com.undefinedus.backend.global.validation.annotation.NoProfanity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscussionUpdateRequestDTO {

    private Long discussionId;  // 수정 할 토론

    @NoProfanity
    private String title;    // 토론 제목

    @NoProfanity
    private String content;     // 토론 주제 글

    private LocalDateTime modifyStartTime;      // 수정한 토론 시작 시간

}
