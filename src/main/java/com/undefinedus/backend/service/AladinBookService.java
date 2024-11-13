package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.dto.request.book.AladinBookRequestDTO;
import java.util.Optional;

public interface AladinBookService {

    Optional<AladinBook> existsAladinBook(String isbn13);

    AladinBook insertAladinBook(AladinBookRequestDTO requestDTO);
}
