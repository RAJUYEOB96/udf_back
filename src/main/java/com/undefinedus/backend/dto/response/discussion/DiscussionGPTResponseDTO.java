package com.undefinedus.backend.dto.response.discussion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // 기본 생성자 추가
@AllArgsConstructor
@Builder
public class DiscussionGPTResponseDTO {

    private String conclusion;  // AI가 분석한 토론 결론/요약

    private Boolean result;    // AI가 분석한 최종 결과 (true: 찬성 우세, false: 반대 우세, null: 판단불가)

    private Integer agreePercent; // AI가 분석한 결과 퍼센트를 실수 형태가 아닌 정수형태로(여긴 찬성 쪽) 받을때 그렇게 받기

    private Integer disagreePercent; // 여긴 반대 우리가 계산해서 넣기 (100 - agreePercent)

    private String reasoning;  // AI의 결과 도출 근거
}
