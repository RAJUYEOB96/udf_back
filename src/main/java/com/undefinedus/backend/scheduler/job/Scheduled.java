package com.undefinedus.backend.scheduler.job;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.exception.discussion.DiscussionNotFoundException;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.scheduler.config.QuartzConfig;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Log4j2
@Component
@RequiredArgsConstructor
public class Scheduled implements Job {

    private final DiscussionRepository discussionRepository;
    private final QuartzConfig quartzConfig;

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
                quartzConfig.removeJob(discussionId);
                // 관련된 discussion 소프트 딜리트
                discussion.changeDeleted(true);
                discussion.changeDeletedAt(LocalDateTime.now());
            }


        } catch (NumberFormatException e) {
            log.error("잘못된 숫자 형식입니다. discussionId: {}", discussionIdStr, e);
            throw new JobExecutionException("잘못된 숫자 형식", e);
        } catch (Exception e) {
            log.error("토론 상태 변경 중 오류 발생. 토론 ID: {}", discussionIdStr, e);
            throw new JobExecutionException("상태 변경 중 오류 발생", e);
        }

    }
}