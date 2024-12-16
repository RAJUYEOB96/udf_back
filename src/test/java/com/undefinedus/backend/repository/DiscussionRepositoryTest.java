package com.undefinedus.backend.repository;

import static org.junit.jupiter.api.Assertions.*;

import com.undefinedus.backend.domain.entity.Discussion;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DiscussionRepositoryTest {

    @Autowired
    private DiscussionRepository discussionRepository;

    @Test
    @DisplayName("연결 확인")
    void testConnection() {
        assertNotNull(discussionRepository);
    }

    @Test
    @DisplayName("토론 게시물 아이디 및 참여 인원 조회")
    public void findByIdWithParticipants() {

        Long discussionId = 1L;

        Optional<Discussion> byIdWithParticipants = discussionRepository.findByIdWithParticipants(
            discussionId);

        System.out.println(byIdWithParticipants);
    }

}