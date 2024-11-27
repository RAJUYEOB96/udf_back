package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.dto.response.discussion.DiscussionGPTResponseDTO;
import com.undefinedus.backend.service.ChatGPTService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatGpt")
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    @GetMapping("/recommended")
    public ResponseEntity<ApiResponseDTO<List<AladinApiResponseDTO>>> getGPTRecommendedBookLIst(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO) {

        Long memberId = memberSecurityDTO.getId();

        System.out.println("memberId : " + memberId);

        List<AladinApiResponseDTO> gptRecommendedBookList = chatGPTService.getGPTRecommendedBookList(
            memberId);

        return ResponseEntity.ok(ApiResponseDTO.success(gptRecommendedBookList));
    }

    @GetMapping("/discussion")
    public ResponseEntity<ApiResponseDTO<DiscussionGPTResponseDTO>> getDiscussionGTP(
        Long discussionId) {

        DiscussionGPTResponseDTO discussionGPTResult = chatGPTService.getDiscussionGPTResult(
            discussionId);

        return ResponseEntity.ok(ApiResponseDTO.success(discussionGPTResult));
    }


}
