package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionGPTResponseDTO;
import java.io.IOException;
import java.util.List;

public interface ChatGPTService {

    List<AladinApiResponseDTO> getGPTRecommendedBookList(Long memberId);

    void discussionInfoToGPT(Long discussionId) throws IOException;

    DiscussionGPTResponseDTO getDiscussionGPTResult(Long discussionId);
}
