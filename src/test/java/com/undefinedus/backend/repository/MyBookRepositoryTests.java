package com.undefinedus.backend.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.ai.openai.api-key=skip")
@SpringBootTest
class MyBookRepositoryTests {

    @Autowired
    private MyBookRepository myBookRepository;

    @Test
    @DisplayName("findTop5Isbn13ByMemberId 테스트")
    public void findTop5Isbn13ByMemberId() {

        Long memberId = 2L;

        List<String> top5Isbn13ByMemberId = myBookRepository.findTop5Isbn13ByMemberId(memberId);

        System.out.println(top5Isbn13ByMemberId);
    }
}