package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.dto.request.book.BookRequestDTO;
import com.undefinedus.backend.dto.request.book.BookStatusRequestDTO;
import com.undefinedus.backend.dto.response.book.BookResponseDTO;
import java.util.List;
import java.util.Map;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BookService {

    boolean existsBook(Long memberId, String isbn13);

    void insertNewBookByStatus(Long memberId, String tabCondition, AladinBook savedAladinBook, BookStatusRequestDTO requestDTO);
}
