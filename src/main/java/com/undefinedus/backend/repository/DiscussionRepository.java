package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionRepository extends JpaRepository<Discussion, Long> {

}
