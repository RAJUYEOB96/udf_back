package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {


    Optional<Book> findByMemberIdAndIsbn13(Long memberId, String isbn13);

}
