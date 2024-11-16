package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.repository.queryDSL.MyBookRepositoryCustom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MyBookRepository extends JpaRepository<MyBook, Long>, MyBookRepositoryCustom {


    Optional<MyBook> findByMemberIdAndIsbn13(Long memberId, String isbn13);
    
    Optional<Object> findByMemberAndAladinBook(Member memberId, AladinBook aladinBookId);   // initData 할때 사용하고 있음
    
    Optional<MyBook> findByIdAndMemberId(Long bookId, Long memberId);
    
    @Query("select mb from MyBook mb "
            + "left join fetch mb.member m "
            + "left join fetch mb.aladinBook a "
            + "where mb.id = :bookId "
            + "and m.id = :memberId ")
    Optional<MyBook> findByIdAndMemberIdWithAladinBook(@Param("bookId") Long bookId, @Param("memberId") Long memberId);
    
    void deleteByIdAndMemberId(Long bookId, Long memberid);
    
    @Query(nativeQuery = true,
            value = "SELECT ab.isbn13 FROM my_book mb " +
                    "JOIN member m ON mb.member_id = m.id " +
                    "JOIN aladin_book ab ON mb.isbn13 = ab.isbn13 " +
                    "WHERE m.id = :memberId " +
                    "ORDER BY ab.customer_review_rank DESC " +
                    "LIMIT 5")
    List<String> findTop5Isbn13ByMemberId(@Param("memberId") Long memberId);
}
