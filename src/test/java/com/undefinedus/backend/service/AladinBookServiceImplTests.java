package com.undefinedus.backend.service;

import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@Log4j2
class AladinBookServiceImplTests {

    @Autowired
    private AladinBookService aladinBookService;

    @Test
    @DisplayName("알라딘 api 검색 호출 테스트")
    public void searchKeywordAladinAPITest() {

        String keyword = "삼국지";

        int page = 1;

        String sort = "Title";

        Map<String, Object> aladinApiResponseDTOS = aladinBookService.searchKeywordAladinAPI(
            page,
            keyword,
            sort);


        // 결과를 반복하여 출력
        System.out.println(aladinApiResponseDTOS);
    }

    @Test
    @DisplayName("알라딘 api 베스트셀러 호출 테스트")
    public void searchBestsellerAladinAPIList() {

        List<AladinApiResponseDTO> aladinApiResponseDTOS = aladinBookService.searchBestsellerAladinAPIList();

        for (AladinApiResponseDTO aladinApiResponseDTO : aladinApiResponseDTOS) {
            System.out.println("aladinApiResponseDTO = " + aladinApiResponseDTO);
            System.out.println();
        }
    }

    @Test
    @DisplayName("알라딘 api 에디터 초이스 호출 테스트")
    public void searchEditorChoiceAladinAPIListTest() {

        Long memberId = 2L;

        Map<String, List<AladinApiResponseDTO>> aladinApiResponseDTOS = aladinBookService.searchEditorChoiceAladinAPIList(
            memberId);

        int count = 0;

        // 결과를 반복하여 출력
        for (Map.Entry<String, List<AladinApiResponseDTO>> entry : aladinApiResponseDTOS.entrySet()) {
            List<AladinApiResponseDTO> books = entry.getValue();

            for (AladinApiResponseDTO book : books) {
                count++;

                System.out.println("count = " + count + " book = " + book);
            }
        }
    }

    @Test
    @DisplayName("getDetailAladinAPI 메서드 테스트")
    void testGetDetailAladinAPI(){

        Long memberId = 2L;

        String isbn13 = "9788936434120";

        List<AladinApiResponseDTO> findList = aladinBookService.getDetailAladinAPI(memberId,
            isbn13);

        for (AladinApiResponseDTO aladinApiResponseDTO : findList) {
            System.out.println("aladinApiResponseDTO = " + aladinApiResponseDTO);
            System.out.println();
        }

    }
}