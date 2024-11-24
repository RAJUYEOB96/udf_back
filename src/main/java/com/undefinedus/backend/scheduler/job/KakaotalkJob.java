package com.undefinedus.backend.scheduler.job;

import com.undefinedus.backend.service.AladinBookService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
public class KakaotalkJob implements Job {

    @Autowired
    private AladinBookService aladinBookService;

    // 기본 생성자 추가
    public KakaotalkJob() {
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 실행할 작업 내용

        // todo: 카카오톡으로 메시지 보내는 작업 해야 함.
        System.out.println("카카오톡 메시지거 전송 되었습니다!");
        System.out.println("실행 시간: " + LocalDateTime.now());
    }
}
