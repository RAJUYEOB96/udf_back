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
    private String fullDescription2; // gpt에게 책의 추가적인 정보를 넘겨주기 위해 필요
    private String bestRank;   // 베스트셀러 순위 정보

    private SubInfoDTO subInfo;

    // 나중에 있는지 없는지 확인해서 넣어줄 예정
    private String status;

    private String category;
}
