package com.undefinedus.backend.scheduler.job;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.exception.discussion.DiscussionNotFoundException;
import com.undefinedus.backend.repository.DiscussionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class Completed implements Job {

    private final DiscussionRepository discussionRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String discussionIdStr = dataMap.getString("discussionId");

        Long discussionId = Long.parseLong(discussionIdStr);

        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(
            () -> new DiscussionNotFoundException("해당 토론을 찾지 못했습니다. : " + discussionId));


        discussion.changeStatus(DiscussionStatus.COMPLETED);

        discussionRepository.save(discussion);

        log.info("{}번 토론이 {}상태로 변경 되었습니다.", discussion.getId(), discussion.getStatus());
    }
}