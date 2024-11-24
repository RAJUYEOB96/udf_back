package com.undefinedus.backend.dto.request.discussion;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiscussionRegisterRequestDTO {

    private Long myBook;  // 어떤 책의 토론인지 // 내가 기록한 책만 토론 주제로 올릴 수 있음

    private Long member;  // 작성자

    private String title;    // 토론 제목

    private String content;

    private int agree; // 토론 찬성 반대 참여자 수를 세기 위해 필요 예(찬성 2)

    private int disagree; // 토론 찬성 반대 참여자 수를 세기 위해 필요 예(반대 2)

    private LocalDateTime startDate; // 토론을 시작할 시간 // 토론 시작 시간은 createdDate보다 최소 24시간 뒤 최대 7일 이여야 한다.
}
