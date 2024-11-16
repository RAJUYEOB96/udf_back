package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.service.AladinBookService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/api/aladinApi")
public class AladinAPIController {

    private final AladinBookService aladinBookService;

    @GetMapping("/keyword")
    public ResponseEntity<List<AladinApiResponseDTO>> getKeywordAladinAPIList(
        @RequestParam(name = "keyword") String keyword) {

        List<AladinApiResponseDTO> aladinApiResponseDTOS = aladinBookService.searchKeywordAladinAPI(
            keyword);

        return ResponseEntity.ok(aladinApiResponseDTOS);
    }

    @GetMapping("/bestseller")
    public ResponseEntity<List<AladinApiResponseDTO>> getBestsellerAladinAPIList() {

        List<AladinApiResponseDTO> bestsellerAladinAPIList = aladinBookService.searchBestsellerAladinAPIList();

        return ResponseEntity.ok(bestsellerAladinAPIList);
    }

    @GetMapping("/editorChoice/{memberId}")
    public ResponseEntity<Map<String, List<AladinApiResponseDTO>>> getEditorChoiceAladinAPIList(
        @PathVariable("memberId") Long memberId) {

        Map<String, List<AladinApiResponseDTO>> editorChoiceAladinAPIList = aladinBookService.searchEditorChoiceAladinAPIList(
            memberId);

        return ResponseEntity.ok(editorChoiceAladinAPIList);
    }

    @PostMapping("/detail/{memberId}")
    public ResponseEntity<List<AladinApiResponseDTO>> getDetailAladinAPI(
        @PathVariable("memberId") Long memberId, @RequestParam("isbn13") String isbn13) {

        List<AladinApiResponseDTO> detailAladinAPI = aladinBookService.getDetailAladinAPI(memberId,
            isbn13);

        return ResponseEntity.ok(detailAladinAPI);
    }


}
