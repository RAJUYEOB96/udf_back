package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.AladinBook;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.repository.queryDSL.MyBookRepositoryCustom;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MyBookRepository extends JpaRepository<MyBook, Long>, MyBookRepositoryCustom {


    Optional<MyBook> findByMemberIdAndIsbn13(Long memberId, String isbn13);

    Optional<Object> findByMemberAndAladinBook(Member memberId,
        AladinBook aladinBookId);   // initData 할때 사용하고 있음

    Optional<MyBook> findByIdAndMemberId(Long bookId, Long memberId);

    @Query("select mb from MyBook mb "
        + "left join fetch mb.member m "
        + "left join fetch mb.aladinBook a "
        + "where mb.id = :bookId "
        + "and m.id = :memberId ")
    Optional<MyBook> findByIdAndMemberIdWithAladinBook(@Param("bookId") Long bookId,
        @Param("memberId") Long memberId);

    void deleteByIdAndMemberId(Long bookId, Long memberid);

    @Query(nativeQuery = true,
        value = "SELECT ab.isbn13 FROM my_book mb " +
            "JOIN member m ON mb.member_id = m.id " +
            "JOIN aladin_book ab ON mb.isbn13 = ab.isbn13 " +
            "WHERE m.id = :memberId " +
            "ORDER BY ab.customer_review_rank DESC, ab.title ASC " +
            "LIMIT 5")
    List<String> findTop5Isbn13ByMemberId(@Param("memberId") Long memberId);

    Set<MyBook> findByMemberId(Long loginMemberId);
    
    // 아래는 initData할때 필요한 sql, 추후 삭제 될 수 있음
    @Query("SELECT m FROM MyBook m JOIN FETCH m.aladinBook WHERE m.status IN ('READING', 'COMPLETED')")
    List<MyBook> findAllWithAladinBook();

    // 통계 관련 쿼리

    // 카테고리 별 읽은 책 권 수
    @Query("SELECT a.categoryName, COUNT(m) " +
        "FROM MyBook m " +
        "JOIN m.aladinBook a " +
        "WHERE m.status = 'COMPLETED' AND m.member.id = :memberId " +
        "GROUP BY a.categoryName")
    List<Object[]> findCompletedBooksGroupedByCategory(@Param("memberId") Long memberId);

    // 연도 별 읽은 책 권 수 // 평균 내는 것에도 사용 // AND YEAR(m.end_date) >= (YEAR(CURDATE()) - 2) 최근 3년
    @Query(nativeQuery = true,
        value = "SELECT YEAR(m.end_date), COUNT(m.id) " +
            "FROM my_book m " +
            "WHERE m.status = 'COMPLETED' " +
            "AND m.member_id = :memberId " +
            "AND YEAR(m.end_date) >= (YEAR(CURDATE()) - 2) " +
            "GROUP BY YEAR(m.end_date) " +
            "ORDER BY YEAR(m.end_date) DESC")
    List<Object[]> findCompletedBooksGroupedByYear(@Param("memberId") Long memberId);

    // 연_월 별 읽은 책 권 수
    @Query(nativeQuery = true,
        value = "SELECT CONCAT(YEAR(m.end_date), '-', "
            + "       CASE "
            + "           WHEN MONTH(m.end_date) < 10 THEN CONCAT('0', MONTH(m.end_date)) "
            + "           ELSE CAST(MONTH(m.end_date) AS VARCHAR) "
            + "       END) AS year_month, "
            + "       COUNT(m.id) "
            + "FROM my_book m "
            + "WHERE m.status = 'COMPLETED' "
            + "AND m.member_id = :memberId "
            + "AND YEAR(m.end_date) >= (YEAR(CURDATE()) - 2) "
            + "GROUP BY YEAR(m.end_date), MONTH(m.end_date) "
            + "ORDER BY YEAR(m.end_date) DESC, MONTH(m.end_date) DESC")
    List<Object[]> findCompletedBooksGroupedByYearAndMonth(@Param("memberId") Long memberId);

    // 각 연도별 총 페이지 수
    @Query(nativeQuery = true,
        value = "select YEAR(m.end_date), sum(m.current_page) "
            + "from my_book m "
            + "where m.member_id = :memberId "
            + "AND m.status = 'COMPLETED'"
            + "AND YEAR(m.end_date) >= (YEAR(CURDATE()) - 2) "
            + "GROUP BY YEAR(m.end_date) "
            + "ORDER BY YEAR(m.end_date) DESC")
    List<Object[]> finCompletedBookPageGroupedByYear(@Param("memberId") Long memberId);
}
