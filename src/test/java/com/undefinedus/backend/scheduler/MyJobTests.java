package com.undefinedus.backend.scheduler;

import com.undefinedus.backend.domain.entity.MyBook;
import com.undefinedus.backend.domain.enums.BookStatus;
import com.undefinedus.backend.repository.MyBookRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MyJobTests {

    @Autowired
    private MyBookRepository myBookRepository;

    // 기본 생성자 추가
    public void MyJob() {
        this.myBookRepository = null; // 또는 적절한 초기화
    }

    @Test
    public void execute() {
        Long memberId = 2L;

        Long bookId = 2l;

        Optional<MyBook> byMemberIdAndId = myBookRepository.findByIdAndMemberId(bookId, memberId);

        System.out.println("byMemberIdAndId = " + byMemberIdAndId);

        byMemberIdAndId.get().setStatus(BookStatus.valueOf("STOPPED"));

        System.out.println("byMemberIdAndId = " + byMemberIdAndId);

        System.out.println("작업이 실행되었습니다!");
        System.out.println("실행 시간: " + LocalDateTime.now());
    }
}