package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.dto.request.book.AladinBookRequestDTO;
import com.undefinedus.backend.dto.response.aladinAPI.AladinApiResponseDTO;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AladinBookService {

    Optional<AladinBook> existsAladinBook(String isbn13);

    AladinBook insertAladinBook(AladinBookRequestDTO requestDTO);

    Map<String, Object> searchKeywordAladinAPI(Integer page,
        String search, String sort, Long memberId);

    List<AladinApiResponseDTO> searchBestsellerAladinAPIList();

    Map<String, List<AladinApiResponseDTO>> searchEditorChoiceAladinAPIList(Long memberId);

    List<AladinApiResponseDTO> getDetailAladinAPI(Long memberId, String isbn13);

    List<AladinApiResponseDTO> detailAladinAPI(String isbn13);
}
