package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionGPTResponseDTO;
import java.io.IOException;
import java.util.List;

public interface AiService {

    List<AladinApiResponseDTO> getPerplexityRecommendBookList(Long memberId);

    void discussionInfoToGPT(Long discussionId) throws IOException;

    DiscussionGPTResponseDTO getDiscussionGPTResult(Long discussionId);
}
