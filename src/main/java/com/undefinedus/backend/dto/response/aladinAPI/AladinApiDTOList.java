package com.undefinedus.backend.dto.response.aladinAPI;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
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
public class AladinApiDTOList {

    private Long totalResults;

    @JsonProperty("item")
    private List<AladinApiResponseDTO> item;
}
