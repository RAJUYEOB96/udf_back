package com.undefinedus.backend.scheduler.job;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.exception.discussion.DiscussionNotFoundException;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.scheduler.repository.QuartzJobDetailRepository;
import com.undefinedus.backend.scheduler.repository.QuartzTriggerRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@RequiredArgsConstructor
public class Scheduled implements Job {

    private final DiscussionRepository discussionRepository;

    private final QuartzJobDetailRepository quartzJobDetailRepository;

    private final QuartzTriggerRepository quartzTriggerRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println("Scheduled");
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String discussionIdStr = dataMap.getString("discussionId");

        Long discussionId = Long.parseLong(discussionIdStr);

        Discussion discussion = discussionRepository.findById(discussionId).orElseThrow(
            () -> new DiscussionNotFoundException("해당 토론을 찾지 못했습니다. : " + discussionId));

        try {
            // 참가자의 동의 및 반대 수 계산
            Long agreeCount = discussion.getParticipants().stream()
                .filter(participant -> participant.isAgree() == true).count();
            Long disagreeCount = discussion.getParticipants().size() - agreeCount;

            // 조건에 맞으면 상태 변경
            if (agreeCount >= 2 && disagreeCount >= 2) {

                Discussion discussionForChangeStatus = discussionRepository.findById(discussionId)
                    .orElseThrow();

                discussionForChangeStatus.changeStatus(DiscussionStatus.SCHEDULED);

                discussionRepository.save(discussionForChangeStatus);

            } else {

                // 관련된 Job, discussion 삭제
                removeJob(discussionId, DiscussionStatus.SCHEDULED);
                removeJob(discussionId, DiscussionStatus.IN_PROGRESS);
                removeJob(discussionId, DiscussionStatus.ANALYZING);
                removeJob(discussionId, DiscussionStatus.COMPLETED);
            }


        } catch (NumberFormatException e) {
            log.error("잘못된 숫자 형식입니다. discussionId: {}", discussionIdStr, e);
            throw new JobExecutionException("잘못된 숫자 형식", e);
        } catch (Exception e) {
            log.error("토론 상태 변경 중 오류 발생. 토론 ID: {}", discussionIdStr, e);
            throw new JobExecutionException("상태 변경 중 오류 발생", e);
        }

    }

    public void removeJob(Long discussionId, DiscussionStatus newStatus) throws SchedulerException {
        // Job 이름을 기준으로 작업 삭제
        String jobName = "discussion_" + discussionId.toString() + "_";
        String schedName = "discussion ID : ";
        
        // DB에서 관련된 Quartz 작업과 트리거 삭제
        quartzJobDetailRepository.deleteByJobName(jobName + DiscussionStatus.SCHEDULED);  // Job 이름을 기준으로 삭제
        quartzJobDetailRepository.deleteByJobName(jobName + DiscussionStatus.IN_PROGRESS);  // Job 이름을 기준으로 삭제
        quartzJobDetailRepository.deleteByJobName(jobName + DiscussionStatus.ANALYZING);  // Job 이름을 기준으로 삭제
        quartzJobDetailRepository.deleteByJobName(jobName + DiscussionStatus.COMPLETED);  // Job 이름을 기준으로 삭제

        quartzTriggerRepository.deleteByTriggerName(schedName + discussionId);  // Trigger 이름을 기준으로 삭제
    }
}