package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Book;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {


    @Query("select b from Book b where b.member.id = :memberId and b.isDeleted = false")
    Optional<Book> findMemberBook(@Param("memberId") Long memberId);

}
