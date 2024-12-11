package com.undefinedus.backend.repository;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.entity.DiscussionParticipant;
import com.undefinedus.backend.domain.entity.Member;
import com.undefinedus.backend.domain.entity.Report;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscussionParticipantRepository extends JpaRepository<DiscussionParticipant, Long> {
    Optional<DiscussionParticipant> findByDiscussionAndMember(Discussion discussion, Member member);
    
    Boolean existsByMemberIdAndDiscussionId(Long loginMemberId, Long id);
    
    Optional<DiscussionParticipant> findByMemberIdAndDiscussionId(Long loginMemberId, Long discussionId);
    
    List<DiscussionParticipant> findByDiscussion(Discussion discussion);
}