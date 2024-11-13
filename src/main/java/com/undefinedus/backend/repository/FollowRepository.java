package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {

}
