package com.undefinedus.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.PreferencesType;
import com.undefinedus.backend.dto.request.book.AladinBookRequestDTO;
import com.undefinedus.backend.dto.response.aladinAPI.AladinApiDTOList;
import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import com.undefinedus.backend.repository.AladinBookRepository;
import com.undefinedus.backend.repository.MemberRepository;
import com.undefinedus.backend.repository.MyBookRepository;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class AladinBookServiceImpl implements AladinBookService {

    private final AladinBookRepository aladinBookRepository;

    private final MemberRepository memberRepository;

    private final MyBookRepository myBookRepository;


    //    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<AladinBook> existsAladinBook(String isbn13) {

        Optional<AladinBook> findAladinBook = aladinBookRepository.findByIsbn13(isbn13);

        return findAladinBook;
    }

    @Override
    public AladinBook insertAladinBook(AladinBookRequestDTO requestDTO) {

        AladinBook aladinBook = AladinBook.builder()
            .isbn13(requestDTO.getIsbn13())
            .title(requestDTO.getTitle())
            .author(requestDTO.getAuthor())
            .link(requestDTO.getLink())
            .cover(requestDTO.getCover())
            .fullDescription(requestDTO.getFullDescription())
            .publisher(requestDTO.getPublisher())
            .categoryName(requestDTO.getCategoryName())
            .customerReviewRank(requestDTO.getCustomerReviewRank())
            .itemPage(requestDTO.getItemPage())
            .build();

        return aladinBookRepository.save(aladinBook);
    }

    @Override
    public Map<String, Object> searchKeywordAladinAPI(Integer page,
        String search, String sort) {

        String ttbkey = "ttbrladyd971718002";
        String url = "http://www.aladin.co.kr/ttb/api/ItemSearch.aspx";

        RestTemplate restTemplate = new RestTemplate();
        List<AladinApiResponseDTO> keywordAladinBookNotAdultList = new ArrayList<>();

        try {

                URI uri = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("ttbkey", ttbkey)
                    .queryParam("Query", search)
                    .queryParam("QueryType", "Keyword")
                    .queryParam("MaxResults", 30)
                    .queryParam("start", page)
                    .queryParam("SearchTarget", "Book")
                    .queryParam("output", "js")
                    .queryParam("Version", "20131101")
                    .queryParam("Sort", sort)
                    .build()
                    .toUri();

                AladinApiDTOList response = restTemplate.getForObject(uri,
                    AladinApiDTOList.class);


                // adult가 아닌 책만 필터링하여 리스트에 추가
                List<AladinApiResponseDTO> filteredNotAdultList = response.getItem().stream()
                    .filter(item -> !item.getAdult())
                    .collect(Collectors.toList());

                keywordAladinBookNotAdultList.addAll(filteredNotAdultList);

                log.info("알라딘 API 연결 성공");
            return Map.of("items", keywordAladinBookNotAdultList, "currentPage", page, "totalResults", response.getTotalResults());
//            return Map.of(page, keywordAladinBookNotAdultList);
        } catch (Exception e) {
            log.error("알라딘 API 연결 실패", e);
            return null;
        }
    }

    @Override
    public List<AladinApiResponseDTO> searchBestsellerAladinAPIList() {

        String ttbkey = "ttbrladyd971718002";
        String url = "http://www.aladin.co.kr/ttb/api/ItemList.aspx";

        RestTemplate restTemplate = new RestTemplate();
        List<AladinApiResponseDTO> bestsellerAladinBookNotAdultList = new ArrayList<>();
        int page = 1;

        try {

            while (bestsellerAladinBookNotAdultList.size() < 10) {

                URI uri = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("ttbkey", ttbkey)
                    .queryParam("QueryType", "Bestseller")
                    .queryParam("MaxResults", 10)
                    .queryParam("start", page)
                    .queryParam("SearchTarget", "Book")
                    .queryParam("output", "js")
                    .queryParam("Version", "20131101")
                    .build()
                    .toUri();

                AladinApiDTOList response = restTemplate.getForObject(uri,
                    AladinApiDTOList.class);

                if (response == null || response.getItem().isEmpty()) {
                    // 더 이상 가져올 책이 없으면 반복 종료
                    break;
                }

                // adult가 아닌 책만 필터링하여 리스트에 추가
                List<AladinApiResponseDTO> filteredNotAdultList = response.getItem().stream()
                    .filter(item -> !item.getAdult())
                    .collect(Collectors.toList());

                bestsellerAladinBookNotAdultList.addAll(filteredNotAdultList);

                page++;

                // 현재 수집된 책의 개수가 10개 이상이면 잘라내기
                if (bestsellerAladinBookNotAdultList.size() >= 10) {
                    bestsellerAladinBookNotAdultList = bestsellerAladinBookNotAdultList.subList(0,
                        10);
                    break;
                }

                log.info("알라딘 API 연결 성공:");
            }
            return bestsellerAladinBookNotAdultList;
        } catch (Exception e) {
            log.error("알라딘 API 연결 실패", e);
            return null;
        }
    }

    @Override
    public Map<String, List<AladinApiResponseDTO>> searchEditorChoiceAladinAPIList(Long memberId) {

        Member member = memberRepository.findById(memberId)
            .orElseThrow(
                () -> new IllegalArgumentException("해당 memberId를 찾을 수 없습니다. : " + memberId));

        Set<PreferencesType> preferences = member.getPreferences();

        for (PreferencesType preference : preferences) {
            System.out.println("preference = " + preference);
            System.out.println("preferenceCategoryId = " + preference.getCategoryId());
        }

        String ttbkey = "ttbrladyd971718002";
        String url = "http://www.aladin.co.kr/ttb/api/ItemList.aspx";

        // 결과를 저장할 맵: 키는 categoryId, 값은 해당 카테고리의 책 리스트
        Map<String, List<AladinApiResponseDTO>> categorizedResults = new HashMap<>();
        RestTemplate restTemplate = new RestTemplate();

        for (PreferencesType preference : preferences) {
            List<AladinApiResponseDTO> booksForCategoryNotAdult = new ArrayList<>();
            int page = 1;

            while (booksForCategoryNotAdult.size() <= 10) {
                URI uri = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("ttbkey", ttbkey)
                    .queryParam("QueryType", "ItemEditorChoice")
                    .queryParam("CategoryId", preference.getCategoryId())
                    .queryParam("MaxResults", 10)
                    .queryParam("start", page)
                    .queryParam("SearchTarget", "Book")
                    .queryParam("output", "js")
                    .queryParam("Version", "20131101")
                    .build()
                    .toUri();

                try {
                    AladinApiDTOList response = restTemplate.getForObject(uri,
                        AladinApiDTOList.class);

                    if (response == null || response.getItem().isEmpty()) {
                        // 더 이상 가져올 책이 없으면 반복 종료
                        break;
                    }

                    // 성인 도서를 제외하고, adult=false인 도서만 필터링
                    List<AladinApiResponseDTO> finalBooksForCategoryNotAdult = booksForCategoryNotAdult;
                    response.getItem().stream()
                        .filter(item -> !item.getAdult())
                        .forEach(responseDTO -> {
                            responseDTO.setCategoryId(preference.getCategoryId());
                            finalBooksForCategoryNotAdult.add(responseDTO);
                        });

                    // 페이지 증가
                    page++;

                    // 현재 수집된 책의 개수가 10개 이상이면 더 이상 페이지를 넘기지 않음
                    if (booksForCategoryNotAdult.size() >= 10) {
                        // `subList`를 사용하지 않고, 10개 이상의 책이 모이면 그만두기
                        booksForCategoryNotAdult = new ArrayList<>(booksForCategoryNotAdult.subList(0, 10)); // 새로운 ArrayList로 복사
                        break;
                    }

                } catch (Exception e) {
                    log.error("알라딘 API 연결 실패 : ", e.getMessage());
                    break;
                }
            }

            // 카테고리 ID를 키로 책 리스트 저장
            categorizedResults.put(preference.getCategoryId().toString(), booksForCategoryNotAdult);
            log.info("알라딘 API 연결 성공 - 카테고리 ID: " + preference.getCategoryId());
        }
        return categorizedResults;
    }


    @Override
    public List<AladinApiResponseDTO> getDetailAladinAPI(Long memberId, String isbn13) {
        List<AladinApiResponseDTO> aladinApiResponseDTO = detailAladinAPI(isbn13);
        if (aladinApiResponseDTO.isEmpty()) {
            return new ArrayList<>();
        }
        return checkMyBooksFromAladinAPI(memberId, aladinApiResponseDTO);
    }

    // ChatGPT메서드에서 사용하기 때문에 퍼블릭으로 함
    @Override
    public List<AladinApiResponseDTO> detailAladinAPI(String isbn13) {

        String ttbkey = "ttbrladyd971718002";
        String url = "http://www.aladin.co.kr/ttb/api/ItemLookUp.aspx";

        URI uri = UriComponentsBuilder.fromHttpUrl(url)
            .queryParam("ttbkey", ttbkey)
            .queryParam("itemIdType", "ISBN13")
            .queryParam("ItemId", isbn13)
            .queryParam("output", "js")
            .queryParam("Version", "20131101")
            .queryParam("OptResult", "fulldescription")
            .build()
            .toUri();

        try {
            RestTemplate restTemplate = new RestTemplate();
            AladinApiDTOList response = restTemplate.getForObject(uri, AladinApiDTOList.class);

            if (response != null) {
                return response.getItem(); // List<AladinApiResponseDTO> 반환
            }
            log.error("알라딘 API 응답이 null입니다.");
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error("알라딘 API 연결 실패", e);
            return null;
        }
        return new ArrayList<>();  // null 대신 빈 리스트 반환
    }

    private List<AladinApiResponseDTO> checkMyBooksFromAladinAPI(
        Long memberId,
        List<AladinApiResponseDTO> aladinApiResponseDTOList) {

        List<String> isbnList = aladinApiResponseDTOList.stream()
            .map(AladinApiResponseDTO::getIsbn13)
            .collect(Collectors.toList());

        for (String isbn : isbnList) {
            Optional<MyBook> myBook = myBookRepository.findByMemberIdAndIsbn13(
                memberId, isbn);

            if (myBook.isPresent()) {

                AladinApiResponseDTO book = aladinApiResponseDTOList.stream()
                    .filter(a -> a.getIsbn13().equals(isbn))
                    .findFirst()
                    .orElse(null);

                if (book != null) {
                    book.setStatus(String.valueOf(myBook.get().getStatus()));
                }
            }

        }

        return aladinApiResponseDTOList;
    }

}