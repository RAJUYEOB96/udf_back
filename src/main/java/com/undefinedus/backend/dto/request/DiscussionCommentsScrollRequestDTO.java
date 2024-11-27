package com.undefinedus.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscussionCommentsScrollRequestDTO {
    
    // 검색의 searchType은  작가/책 제목 둘다 동시에 할 예정
    
    @Builder.Default
    private Long lastId = 0L; // 마지막으로 로드된 항목의 ID
    
    @Builder.Default
    private int size = 10;    // 한 번에 로드할 항목 수
    
    @Builder.Default
    private String sort = "asc"; // asc : 오름차순, desc : 내림차순

    // 추가 필터링이나 검색 조건을 위한 필드들을 여기에 추가
}
