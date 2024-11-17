package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.MyBookmark;
import com.undefinedus.backend.repository.queryDSL.MyBookmarkRepositoryCustom;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyBookmarkRepository extends JpaRepository<MyBookmark, Long>, MyBookmarkRepositoryCustom {
    
    Optional<MyBookmark> findByIdAndMemberId(Long bookmarkId, Long memberId);
}
