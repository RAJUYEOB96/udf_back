package com.undefinedus.backend.controller;

import com.undefinedus.backend.dto.MemberSecurityDTO;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // 키워드 검색
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

    // 베스트 셀러
    @GetMapping("/bestseller")
    public ResponseEntity<ApiResponseDTO<List<AladinApiResponseDTO>>> getBestsellerAladinAPIList() {

        List<AladinApiResponseDTO> bestsellerAladinAPIList = aladinBookService.searchBestsellerAladinAPIList();

        return ResponseEntity.ok(ApiResponseDTO.success(bestsellerAladinAPIList));
    }

    // 회원의 카테고리에 따라 추천(에디터 추천 api)
    @GetMapping("/editorChoice")
    public ResponseEntity<ApiResponseDTO<Map<String, List<AladinApiResponseDTO>>>> getEditorChoiceAladinAPIList(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO) {

        Long memberId = memberSecurityDTO.getId();

        Map<String, List<AladinApiResponseDTO>> editorChoiceAladinAPIList = aladinBookService.searchEditorChoiceAladinAPIList(
            memberId);

        return ResponseEntity.ok(ApiResponseDTO.success(editorChoiceAladinAPIList));
    }

    // 책 상세
    @GetMapping("/detail")
    public ResponseEntity<ApiResponseDTO<List<AladinApiResponseDTO>>> getDetailAladinAPI(
        @AuthenticationPrincipal MemberSecurityDTO memberSecurityDTO,
        @RequestParam("isbn13") String isbn13) {

        Long memberId = memberSecurityDTO.getId();

        List<AladinApiResponseDTO> detailAladinAPI = aladinBookService.getDetailAladinAPI(
            memberId, isbn13);

        return ResponseEntity.ok(ApiResponseDTO.success(detailAladinAPI));
    }
}
