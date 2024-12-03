package com.undefinedus.backend.scheduler.job;

import com.undefinedus.backend.service.KakaoTalkService;
import jakarta.transaction.Transactional;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Transactional
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
