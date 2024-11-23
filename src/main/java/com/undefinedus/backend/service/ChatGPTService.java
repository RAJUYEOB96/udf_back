package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import java.util.List;

public interface ChatGPTService {

    List<AladinApiResponseDTO> getGPTRecommendedBookList(Long memberId);
}
