package com.undefinedus.backend.dto.response.bookmark;

import com.undefinedus.backend.domain.entity.MyBookmark;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MyBookmarkResponseDTO {
    
    private Long id;                  // ID
    
    private String title;               // 도서 제목
    
    private Integer pageNumber;       // 구절이 기록된 페이지
    
    private String phrase;              // 기록할 구절
    
    private LocalDate recordDate;       // 기록된 날짜
    
    private Integer totalPageNumber;  // 책이 가지고 있는 totalPageNumber
    
    // 아래는 필요없지만 추가로 필요 할 수도 있는 필드들
    
    private String cover;               // 표지 이미지 URL
    
    public static MyBookmarkResponseDTO from(MyBookmark myBookmark) {
        return MyBookmarkResponseDTO.builder()
                .id(myBookmark.getId())
                .title(myBookmark.getAladinBook().getTitle())
                .pageNumber(myBookmark.getPageNumber())
                .phrase(myBookmark.getPhrase())
                .recordDate(myBookmark.getCreatedDate().toLocalDate())
                .totalPageNumber(myBookmark.getAladinBook().getItemPage())
                .cover(myBookmark.getAladinBook().getCover())
                .build();
    }
}
