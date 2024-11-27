package com.undefinedus.backend.repository;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.undefinedus.backend.config.QueryDSLConfig;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(QueryDSLConfig.class)  // 이 부분 추가
class MyBookRepositoryTests {

    @Autowired
    private MyBookRepository myBookRepository;

    @Test
    @DisplayName("findTop5Isbn13ByMemberId 테스트")
    public void findTop5Isbn13ByMemberId() {

        Long memberId = 2L;

        List<String> top5Isbn13ByMemberId = myBookRepository.findTop5Isbn13ByMemberId(memberId);
        
        assertNotNull(top5Isbn13ByMemberId);
    }
}