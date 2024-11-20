package com.undefinedus.backend.dto.response.aladinAPI;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AladinApiResponseDTO {
    private String title;
    private String link;
    private String author;
    private String isbn13;
    private String cover;
    private String categoryName;
    private String publisher;
    private Boolean adult;
    private Double customerReviewRank;
    private String fullDescription;

    private SubInfoDTO subInfo;

    // 나중에 있는지 없는지 확인해서 넣어줄 예정
    private String status;

    // 카테고리가 너무 세분화 되어 있어 분류가 힘듬
    // 검색했을 때의 아이디 값으로 분류 할 수 있도록 하는 것
    private Integer categoryId;
}
