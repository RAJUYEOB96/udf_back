package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.dto.request.BookScrollRequestDTO;
import com.undefinedus.backend.dto.request.book.BookStatusRequestDTO;
import com.undefinedus.backend.dto.response.ScrollResponseDTO;
import com.undefinedus.backend.dto.response.book.MyBookResponseDTO;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MyBookService {

    boolean existsBook(Long memberId, String isbn13);

    void insertNewBookByStatus(Long memberId, AladinBook savedAladinBook, BookStatusRequestDTO requestDTO);
    
    void updateBookStatus(Long memberId, Long bookId, BookStatusRequestDTO requestDTO);
    
    ScrollResponseDTO<MyBookResponseDTO> getMyBookList(Long memberId, BookScrollRequestDTO requestDTO);
    
    MyBookResponseDTO getMyBook(Long memberId, Long bookId);
}
