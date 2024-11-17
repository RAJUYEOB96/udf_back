package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.service.ChatGPTService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatGpt")
public class ChatGPTController {

    private final ChatGPTService chatGPTService;

    @GetMapping("{memberId}")
    public ResponseEntity<List<AladinApiResponseDTO>> getGPTRecommendedBookLIst(
        @PathVariable("memberId") Long memberId) {

        List<AladinApiResponseDTO> gptRecommendedBookLIst = chatGPTService.getGPTRecommendedBookLIst(
            memberId);

        return ResponseEntity.ok(gptRecommendedBookLIst);
    }
}
