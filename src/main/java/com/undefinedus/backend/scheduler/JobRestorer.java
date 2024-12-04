package com.undefinedus.backend.scheduler;

import com.undefinedus.backend.domain.entity.Discussion;
import com.undefinedus.backend.domain.enums.DiscussionStatus;
import com.undefinedus.backend.repository.DiscussionRepository;
import com.undefinedus.backend.scheduler.entity.QuartzTrigger;
import com.undefinedus.backend.scheduler.repository.QuartzTriggerRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
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

        List<Discussion> discussionList = discussionRepository.findAll(); // DB에서 논의 정보 조회

        for (Discussion discussion : discussionList) {

            if (discussion.getDeletedAt() == null) {

                if (discussion.getStatus() == DiscussionStatus.COMPLETED ||
                    discussion.getStatus() == DiscussionStatus.BLOCKED
                ) {
                    continue;
                }

                // 'PROPOSED' 상태일 때도 이후 상태에 대한 작업을 진행하도록 처리
                List<DiscussionStatus> statusList = getStatusListForProcessing(
                    discussion.getStatus());

                for (DiscussionStatus status : statusList) {

                    try {
                        String jobName =
                            "discussion_" + discussion.getId().toString() + "_" + status;
                        String jobGroup = status.name();
                        String triggerName =
                            "trigger_changeStatus_" + discussion.getId().toString() + "_" + status;
                        String triggerGroup = status.name();

                        // QuartzTrigger에서 트리거 정보 읽기
                        QuartzTrigger quartzTrigger = quartzTriggerRepository.findByTriggerName(
                                triggerName)
                            .orElseThrow(() -> new JobExecutionException(
                                "Trigger not found for " + triggerName));

                        // Trigger 객체 생성 (시간을 `startTime`으로 설정)
                        Trigger trigger = TriggerBuilder.newTrigger()
                            .withIdentity(triggerName, triggerGroup)
                            .startAt(new java.util.Date(quartzTrigger.getStartTime()))  // 시작 시간 설정
                            .build();

                        // JobDetail 생성
                        String jobClassName = getJobClassNameForStatus(status);
                        Class<? extends Job> jobClass = (Class<? extends Job>) Class.forName(
                            jobClassName);

                        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                            .withIdentity(jobName, jobGroup)
                            .usingJobData("discussionId", discussion.getId().toString())
                            .build();

                        // Job과 Trigger를 Scheduler에 등록
                        scheduler.scheduleJob(jobDetail, trigger);
                        System.out.println("Scheduled job for discussion ID: " + discussion.getId()
                            + " with status: " + status);

                    } catch (SchedulerException | ClassNotFoundException e) {
                        System.out.println(
                            "Error scheduling job for discussion ID: " + discussion.getId()
                                + " with status: " + discussion.getStatus());
                        e.printStackTrace(); // 예외 처리
                    }
                }
            }
        }

        try {
            // 스케줄러 시작
            scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace(); // 예외 처리
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
