package com.undefinedus.backend.dto.request.bookmark;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class BookmarkRequestDTO {
    
    private String title; // 받은 다음 백엔드에서 myBook에서 찾을 예정 // isbn13 으로 할걸 그랬나?
    
    private Integer pageNumber; // 책갈피 위치
    
    private String phrase; // 책 구절 내용 (200자)
}
