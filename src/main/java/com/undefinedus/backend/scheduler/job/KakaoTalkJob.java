package com.undefinedus.backend.scheduler.job;

import com.undefinedus.backend.service.KakaoTalkService;
import jakarta.transaction.Transactional;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
@DisallowConcurrentExecution  // 동일한 Job의 여러 인스턴스가 동시에 실행되는 것을 방지합니다.
public class KakaoTalkJob implements Job {

    @Autowired
    private KakaoTalkService kakaoTalkService;

    public KakaoTalkJob() {
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        kakaoTalkService.sendKakaoTalk();
    }
}
