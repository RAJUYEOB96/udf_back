package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.PreferencesType;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MyBookRepository extends JpaRepository<MyBook, Long> {


    Optional<MyBook> findByMemberIdAndIsbn13(Long memberId, String isbn13);

    Optional<Object> findByMemberAndAladinBook(Member reader1, AladinBook dallagut);

    Optional<MyBook> findByMemberIdAndId(Long memberId, Long bookId);

    @Query(nativeQuery = true,
        value = "SELECT ab.isbn13 FROM my_book mb " +
            "JOIN member m ON mb.member_id = m.id " +
            "JOIN aladin_book ab ON mb.isbn13 = ab.isbn13 " +
            "WHERE m.id = :memberId " +
            "ORDER BY ab.customer_review_rank DESC " +
            "LIMIT 5")
    List<String> findTop5Isbn13ByMemberId(@Param("memberId") Long memberId);
}
