package com.undefinedus.backend.scheduler;

import com.undefinedus.backend.repository.MyBookRepository;
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


}