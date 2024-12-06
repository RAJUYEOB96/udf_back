package com.undefinedus.backend.dto.response.aladinAPI;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AladinBookForGPTResponseDTO {

    private String isbn13;

    private String title;

    private String author;

    private String link;

    private String cover;

    private String fullDescription;

    private String fullDescription2;

    private String publisher;

    private String categoryName;

    private Double customerReviewRank;

    private Integer itemPage;

}
