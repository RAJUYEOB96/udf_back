package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.MyBookmark;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MyBookmarkRepository extends JpaRepository<MyBookmark, Long> {

}
