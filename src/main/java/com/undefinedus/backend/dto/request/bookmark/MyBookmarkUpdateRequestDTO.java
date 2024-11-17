package com.undefinedus.backend.dto.request.bookmark;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class MyBookmarkUpdateRequestDTO {
    
    @NotBlank(message = "북마크 문구는 비어있을 수 없습니다")
    private String phrase; // 책 구절 내용 (200자)
    
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    private Integer pageNumber; // 책갈피 위치
    
}
