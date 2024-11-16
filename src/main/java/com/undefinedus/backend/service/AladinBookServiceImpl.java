package com.undefinedus.backend.service;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.dto.request.book.AladinBookRequestDTO;
import com.undefinedus.backend.repository.AladinBookRepository;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class AladinBookServiceImpl implements AladinBookService{

    private final AladinBookRepository aladinBookRepository;

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
}
