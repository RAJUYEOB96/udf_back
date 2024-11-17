package com.undefinedus.backend.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.undefinedus.backend.dto.request.book.AladinBookRequestDTO;
import com.undefinedus.backend.dto.response.aladinAPI.AladinApiDTOList;
import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.dto.response.aladinAPI.SubInfoDTO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@SpringBootTest
class AladinAPIControllerTests {

    @Autowired
    private AladinAPIController aladinAPIController;

    @Test
    @DisplayName("getKeywordAladinAPIList 테스트")
    public void getKeywordAladinAPIListTest() {

        String keyword = "한강";

        ResponseEntity<List<AladinApiResponseDTO>> keywordAladinAPIList = aladinAPIController.getKeywordAladinAPIList(
            keyword);

        System.out.println(keywordAladinAPIList);
    }

    @Test
    @DisplayName("getBestsellerAladinAPIList 테스트")
    public void getBestsellerAladinAPIListTest() {

        ResponseEntity<List<AladinApiResponseDTO>> bestsellerAladinAPIList = aladinAPIController.getBestsellerAladinAPIList();

        List<AladinApiResponseDTO> body = bestsellerAladinAPIList.getBody();

        for (AladinApiResponseDTO book : body) {

            System.out.println(book);
        }
    }

    @Test
    @DisplayName("getEditorChoiceAladinAPIList 테스트")
    public void getEditorChoiceAladinAPIListTest() {
        Long memberId = 6L;

        ResponseEntity<Map<String, List<AladinApiResponseDTO>>> editorChoiceAladinAPIList = aladinAPIController.getEditorChoiceAladinAPIList(
            memberId);

        Map<String, List<AladinApiResponseDTO>> body = editorChoiceAladinAPIList.getBody();

        System.out.println("body = " + body);
    }

    @Test
    @DisplayName("getDetailAladinAPI 테스트")
    public void getDetailAladinAPITest() {
        Long memberId = 6L;

        String isbn13 = "9788936434120";

        ResponseEntity<List<AladinApiResponseDTO>> detailAladinAPI = aladinAPIController.getDetailAladinAPI(
            memberId, isbn13);

        System.out.println(detailAladinAPI);
    }


}