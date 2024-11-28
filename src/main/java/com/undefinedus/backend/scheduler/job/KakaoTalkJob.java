package com.undefinedus.backend.scheduler.job;

import com.undefinedus.backend.scheduler.KakaoTalkSender;
import com.undefinedus.backend.service.AladinBookService;
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
    private AladinBookService aladinBookService;

    // 기본 생성자 추가
    public KakaoTalkJob() {
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 실행할 작업 내용

        // todo: 카카오톡으로 메시지 보내는 작업 해야 함.
        String accessToken = context.getJobDetail().getJobDataMap().getString("accessToken");
        String message = context.getJobDetail().getJobDataMap().getString("message");

        KakaoTalkSender sender = new KakaoTalkSender();
        sender.sendMessage(accessToken, message);
    }
}
