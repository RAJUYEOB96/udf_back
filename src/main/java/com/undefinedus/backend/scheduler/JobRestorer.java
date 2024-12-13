package com.undefinedus.backend.scheduler;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.scheduler.entity.QuartzTrigger;
import com.undefinedus.backend.scheduler.repository.QuartzTriggerRepository;
import jakarta.annotation.PostConstruct;
import java.util.Date;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class JobRestorer {

    @Autowired
    private DiscussionRepository discussionRepository;

    @Autowired
    private Scheduler scheduler; // Quartz Scheduler

    @Autowired
    private QuartzTriggerRepository quartzTriggerRepository;

    @PostConstruct
    public void restoreJobs() {
        List<Discussion> discussionList = discussionRepository.findAll();
        Date now = new Date();

        for (Discussion discussion : discussionList) {
            if (discussion.getDeletedAt() == null &&
                discussion.getStatus() != DiscussionStatus.COMPLETED &&
                discussion.getStatus() != DiscussionStatus.BLOCKED) {

                List<DiscussionStatus> statusList = getStatusListForProcessing(discussion.getStatus());

                for (DiscussionStatus status : statusList) {
                    try {
                        String jobName = "discussion_" + discussion.getId() + "_" + status;
                        String jobGroup = status.name();
                        String triggerName = "trigger_changeStatus_" + discussion.getId() + "_" + status;
                        String triggerGroup = status.name();

                        QuartzTrigger quartzTrigger = quartzTriggerRepository.findByTriggerName(triggerName)
                            .orElseThrow(() -> new JobExecutionException("Trigger not found for " + triggerName));

                        Date startTime = new Date(quartzTrigger.getStartTime());

                        // 시작 시간이 현재 시간보다 이후인 경우에만 스케줄링
                        if (startTime.after(now)) {
                            Trigger trigger = TriggerBuilder.newTrigger()
                                .withIdentity(triggerName, triggerGroup)
                                .startAt(startTime)
                                .build();

                            String jobClassName = getJobClassNameForStatus(status);
                            Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(jobClassName);

                            JobDetail jobDetail = JobBuilder.newJob(jobClass)
                                .withIdentity(jobName, jobGroup)
                                .usingJobData("discussionId", discussion.getId().toString())
                                .build();

                            scheduler.scheduleJob(jobDetail, trigger);

                            log.info("토론 ID: {}에 대한 {} 상태의 작업이 예약되었습니다.", discussion.getId(), status);
                        } else {

                            log.info("토론 ID: {}에 대한 {} 상태의 작업 예약을 건너뛰었습니다. (시작 시간이 이미 지났음)", discussion.getId(), status);
                        }
                    } catch (SchedulerException | ClassNotFoundException e) {

                        log.info("토론 ID: {}에 대한 {} 상태의 작업 예약 중 오류가 발생했습니다.", discussion.getId(), discussion.getStatus());
                        e.printStackTrace();
                    }
                }
            }
        }

        try {
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    // PROPOSED 상태가 있으면 이후 상태도 처리하도록 상태 리스트 반환
    private List<DiscussionStatus> getStatusListForProcessing(DiscussionStatus status) {
        if (status == DiscussionStatus.PROPOSED) {
            return List.of(DiscussionStatus.SCHEDULED, DiscussionStatus.IN_PROGRESS,
                DiscussionStatus.ANALYZING, DiscussionStatus.COMPLETED); // 이후 상태 추가
        } else if (status == DiscussionStatus.SCHEDULED) {
            return List.of(DiscussionStatus.IN_PROGRESS, DiscussionStatus.ANALYZING,
                DiscussionStatus.COMPLETED);
        } else if (status == DiscussionStatus.IN_PROGRESS) {
            return List.of(DiscussionStatus.ANALYZING, DiscussionStatus.COMPLETED);
        } else if (status == DiscussionStatus.ANALYZING) {
            return List.of(DiscussionStatus.COMPLETED);
        } else {
            return List.of(status); // 이미 다른 상태라면 해당 상태만 처리
        }
    }

    // job위치 바뀌면 ""안의 주소 값들도 바꾸어야함
    private String getJobClassNameForStatus(DiscussionStatus status) {
        switch (status) {
            case ANALYZING:
                return "com.undefinedus.backend.scheduler.job.Analyzing";
            case IN_PROGRESS:
                return "com.undefinedus.backend.scheduler.job.InProgress";
            case COMPLETED:
                return "com.undefinedus.backend.scheduler.job.Completed";
            case SCHEDULED:
                return "com.undefinedus.backend.scheduler.job.Scheduled";
            default:
                throw new IllegalArgumentException("Unknown status: " + status);
        }
    }
}
