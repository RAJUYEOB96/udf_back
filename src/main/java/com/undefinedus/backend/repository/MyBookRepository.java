package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyBookRepository extends JpaRepository<MyBook, Long> {


    Optional<MyBook> findByMemberIdAndIsbn13(Long memberId, String isbn13);
    
    Optional<Object> findByMemberAndAladinBook(Member reader1, AladinBook dallagut);
}
