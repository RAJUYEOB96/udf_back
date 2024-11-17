package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.AladinBook;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AladinBookRepository extends JpaRepository<AladinBook, Long> {

    Optional<AladinBook> findByIsbn13(String isbn13);
    
    Optional<AladinBook> findByTitle(String title);
}
