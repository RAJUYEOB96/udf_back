package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.response.ApiResponseDTO;
import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.service.AladinBookService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponseDTO<Map<String, Object>>> getKeywordAladinAPIList(
        @RequestParam(name = "page") Integer page,
        @RequestParam(name = "keyword") String keyword,
        @RequestParam(name = "sort") String sort
    ) {

        Map<String, Object> result = aladinBookService.searchKeywordAladinAPI(
            page, keyword, sort);

        return ResponseEntity.ok(ApiResponseDTO.success(result));
    }

    @GetMapping("/bestseller")
    public ResponseEntity<ApiResponseDTO<List<AladinApiResponseDTO>>> getBestsellerAladinAPIList() {

        List<AladinApiResponseDTO> bestsellerAladinAPIList = aladinBookService.searchBestsellerAladinAPIList();

        return ResponseEntity.ok(ApiResponseDTO.success(bestsellerAladinAPIList));
    }

    @GetMapping("/editorChoice/{memberId}")
    public ResponseEntity<ApiResponseDTO<Map<String, List<AladinApiResponseDTO>>>> getEditorChoiceAladinAPIList(
        @PathVariable("memberId") Long memberId) {

        Map<String, List<AladinApiResponseDTO>> editorChoiceAladinAPIList = aladinBookService.searchEditorChoiceAladinAPIList(
            memberId);

        return ResponseEntity.ok(ApiResponseDTO.success(editorChoiceAladinAPIList));
    }

    @PostMapping("/detail/{memberId}")
    public ResponseEntity<ApiResponseDTO<Void>> getDetailAladinAPI(
        @PathVariable("memberId") Long memberId, @RequestParam("isbn13") String isbn13) {

        try {
            aladinBookService.getDetailAladinAPI(memberId, isbn13);

        } catch (Exception e) {
            log.error("isbn13 조회에서 오류가 발생 했습니다.", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponseDTO.error("책 정보를 조회 할 수 없습니다."));
        }

        return ResponseEntity.status(HttpStatus.OK)
            .body(ApiResponseDTO.success(null));
    }


}
