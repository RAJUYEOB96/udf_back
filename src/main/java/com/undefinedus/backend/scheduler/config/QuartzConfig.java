package com.undefinedus.backend.scheduler.config;

import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.scheduler.entity.QuartzJobDetail;
import com.undefinedus.backend.scheduler.entity.QuartzTrigger;
import com.undefinedus.backend.scheduler.job.Analyzing;
import com.undefinedus.backend.scheduler.job.Completed;
import com.undefinedus.backend.scheduler.job.InProgress;
import com.undefinedus.backend.scheduler.job.KakaoTalkJob;
import com.undefinedus.backend.scheduler.job.Scheduled;
import com.undefinedus.backend.scheduler.repository.QuartzJobDetailRepository;
import com.undefinedus.backend.scheduler.repository.QuartzTriggerRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class QuartzConfig {

    private final QuartzJobDetailRepository quartzJobDetailRepository;
    private final QuartzTriggerRepository quartzTriggerRepository;
    private final Scheduler scheduler;

    public void scheduleDiscussionJobs(LocalDateTime targetTime, Long discussionId)
        throws Exception {

        LocalDateTime now = LocalDateTime.now();
        if (targetTime.isBefore(now)) {
            throw new IllegalArgumentException("타겟 시간이 현재 시간보다 이전입니다.");
        }

        // PROPOSED -> SCHEDULED (시작 3시간 전)
//        LocalDateTime startDateTime = targetTime.minusHours(3);
        LocalDateTime startDateTime = targetTime.minusMinutes(3); // 3분전
        Date startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());

        JobDetail scheduleDetail = JobBuilder.newJob(Scheduled.class)
            .withIdentity("SCHEDULED_" + discussionId.toString())
            .usingJobData("discussionId", discussionId.toString()).build();

        Trigger scheduleTrigger = TriggerBuilder.newTrigger()
            .startAt(startDate)
            .build();

        scheduler.scheduleJob(scheduleDetail, scheduleTrigger);
        saveQuartzDiscussionJobDetail(discussionId, DiscussionStatus.SCHEDULED);
        saveQuartzDiscussionTrigger(discussionId, startDate, DiscussionStatus.SCHEDULED);

        // SCHEDULED -> IN_PROGRESS (시작 시간)
        startDate = Date.from(targetTime.atZone(ZoneId.systemDefault()).toInstant());

        JobDetail inProgressDetail = JobBuilder.newJob(InProgress.class)
            .withIdentity("IN_PROGRESS_" + discussionId.toString())
            .usingJobData("discussionId", discussionId.toString()).build();

        Trigger inProgressTrigger = TriggerBuilder.newTrigger()
            .startAt(startDate)
            .build();

        scheduler.scheduleJob(inProgressDetail, inProgressTrigger);
        saveQuartzDiscussionJobDetail(discussionId, DiscussionStatus.IN_PROGRESS);
        saveQuartzDiscussionTrigger(discussionId, startDate, DiscussionStatus.IN_PROGRESS);

        // IN_PROGRESS -> ANALYZING (시작 24시간 후)
//        startDateTime = targetTime.plusHours(24);
        startDateTime = targetTime.plusSeconds(30); // 30초 뒤
        startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());

        JobDetail analyzingDetail = JobBuilder.newJob(Analyzing.class)
            .withIdentity("ANALYZING_" + discussionId.toString())
            .usingJobData("discussionId", discussionId.toString()).build();

        Trigger analyzingTrigger = TriggerBuilder.newTrigger()
            .startAt(startDate)
            .build();

        scheduler.scheduleJob(analyzingDetail, analyzingTrigger);
        saveQuartzDiscussionJobDetail(discussionId, DiscussionStatus.ANALYZING);
        saveQuartzDiscussionTrigger(discussionId, startDate, DiscussionStatus.ANALYZING);

        // ANALYZING -> COMPLETED (시작 25시간 후, 분석에 1시간 가정)
//        startDateTime = targetTime.plusHours(25);
        startDateTime = targetTime.plusSeconds(30); // 30초 뒤
        startDate = Date.from(startDateTime.atZone(ZoneId.systemDefault()).toInstant());

        JobDetail completedDetail = JobBuilder.newJob(Completed.class)
            .withIdentity("COMPLETED_" + discussionId.toString())
            .usingJobData("discussionId", discussionId.toString()).build();

        Trigger completedTrigger = TriggerBuilder.newTrigger()
            .startAt(startDate)
            .build();

        scheduler.scheduleJob(completedDetail, completedTrigger);
        saveQuartzDiscussionJobDetail(discussionId, DiscussionStatus.COMPLETED);
        saveQuartzDiscussionTrigger(discussionId, startDate, DiscussionStatus.COMPLETED);
    }

    @PostConstruct
    public void scheduleKakaoTalkJob() throws Exception {
        JobDetail kakaoTalkDetail = JobBuilder.newJob(KakaoTalkJob.class)
            .withIdentity("KakaoTalk")
            .build();

        Trigger repeatTrigger = TriggerBuilder.newTrigger()
            .withIdentity("KakaoTalkCronTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 10 10 * * ?")) // 오후 12시
            .build();

        scheduler.scheduleJob(kakaoTalkDetail, repeatTrigger);
    }

    private void saveQuartzDiscussionJobDetail(Long discussionId, DiscussionStatus newStatus) {
        QuartzJobDetail quartzJobDetail = new QuartzJobDetail();
        quartzJobDetail.setSchedName("discussion");
        quartzJobDetail.setJobName("discussion_" + discussionId + "_" + newStatus);
        quartzJobDetail.setJobGroup(newStatus.name());
        String jobClassName = getJobClassNameForStatus(newStatus);
        quartzJobDetail.setJobClassName(jobClassName);
        quartzJobDetailRepository.save(quartzJobDetail);
    }

    private void saveQuartzDiscussionTrigger(Long discussionId, Date executionTime,
        DiscussionStatus newStatus) {
        QuartzTrigger quartzTrigger = new QuartzTrigger();
        quartzTrigger.setTriggerName(
            "trigger_changeStatus_" + discussionId.toString() + "_" + newStatus);
        quartzTrigger.setTriggerGroup(newStatus.name());
        quartzTrigger.setStartTime(executionTime.getTime());
        quartzTrigger.setTriggerType("SIMPLE");
        quartzTrigger.setSchedName("discussion ID : " + discussionId.toString());
        quartzTrigger.setStartTimeEasy(executionTime);

        quartzTriggerRepository.save(quartzTrigger);
    }

    private String getJobClassNameForStatus(DiscussionStatus status) {
        switch (status) {
            case SCHEDULED:
                return Scheduled.class.getName();
            case IN_PROGRESS:
                return InProgress.class.getName();
            case ANALYZING:
                return Analyzing.class.getName();
            case COMPLETED:
                return Completed.class.getName();
            default:
                throw new IllegalArgumentException("Unknown status: " + status);
        }
    }

    public void removeJob(Long discussionId) throws SchedulerException {
        // Job 이름을 기준으로 작업 삭제
        String jobName = "discussion_" + discussionId.toString() + "_";
        String schedName = "discussion ID : ";

        // 메모리에 저장된 스케줄러 삭제
        scheduler.deleteJob(new JobKey("SCHEDULED_" + discussionId));
        scheduler.deleteJob(new JobKey("IN_PROGRESS_" + discussionId));
        scheduler.deleteJob(new JobKey("ANALYZING_" + discussionId));
        scheduler.deleteJob(new JobKey("COMPLETED_" + discussionId));

        // DB에서 관련된 Quartz 작업과 트리거 삭제
        quartzJobDetailRepository.deleteByJobName(
            jobName + DiscussionStatus.SCHEDULED);  // Job 이름을 기준으로 삭제
        quartzJobDetailRepository.deleteByJobName(
            jobName + DiscussionStatus.IN_PROGRESS);  // Job 이름을 기준으로 삭제
        quartzJobDetailRepository.deleteByJobName(
            jobName + DiscussionStatus.ANALYZING);  // Job 이름을 기준으로 삭제
        quartzJobDetailRepository.deleteByJobName(
            jobName + DiscussionStatus.COMPLETED);  // Job 이름을 기준으로 삭제

        quartzTriggerRepository.deleteByTriggerName(
            schedName + discussionId);  // Trigger 이름을 기준으로 삭제
    }
}
