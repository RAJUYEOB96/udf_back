package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.MyBook;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<MyBook, Long> {


    Optional<MyBook> findByMemberIdAndIsbn13(Long memberId, String isbn13);

}
