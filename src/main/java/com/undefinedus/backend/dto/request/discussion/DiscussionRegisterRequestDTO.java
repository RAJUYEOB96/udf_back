package com.undefinedus.backend.dto.request.discussion;

import com.undefinedus.backend.global.validation.annotation.NoProfanity;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscussionRegisterRequestDTO {

    private String isbn13;

    @NoProfanity
    private String title;    // 토론 제목

    @NoProfanity
    private String content;      // 토론 주제 글

    private LocalDateTime startDate; // 토론을 시작할 시간 // 토론 시작 시간은 createdDate보다 최소 24시간 뒤 최대 7일 이여야 한다.
}
