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
public class SubInfoDTO {
    private Integer itemPage;
    private String bestSellerRank;   // 상품의 주간베스트셀러 순위 정보
}
