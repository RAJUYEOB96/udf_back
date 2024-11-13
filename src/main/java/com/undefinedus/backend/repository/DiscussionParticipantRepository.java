package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.DiscussionParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionParticipantRepository extends JpaRepository<DiscussionParticipant, Long> {

}
