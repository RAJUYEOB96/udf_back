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
            .subTitle(requestDTO.getSubTitle())
            .author(requestDTO.getAuthor())
            .summary(requestDTO.getSummary())
            .link(requestDTO.getLink())
            .cover(requestDTO.getCover())
            .description(requestDTO.getDescription())
            .publisher(requestDTO.getPublisher())
            .category(requestDTO.getCategory())
            .customerReviewRank(requestDTO.getCustomerReviewRank())
            .isAdult(requestDTO.isAdult())
            .pagesCount(requestDTO.getPagesCount())
            .build();

        return aladinBookRepository.save(aladinBook);
    }
}
