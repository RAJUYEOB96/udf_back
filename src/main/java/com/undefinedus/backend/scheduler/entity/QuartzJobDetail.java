package com.undefinedus.backend.scheduler.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "QRTZ_JOB_DETAILS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuartzJobDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id; // 고유 식별자로 사용되는 ID

    @Column(name = "SCHED_NAME", length = 120, nullable = false)
    private String schedName; // 스케줄러의 이름 (예: "DEFAULT")

    @Column(name = "JOB_NAME", length = 200, nullable = false)
    private String jobName; // 작업의 이름 (예: "job_1")

    @Column(name = "JOB_GROUP", length = 200, nullable = false)
    private String jobGroup; // 작업이 속한 그룹 (예: "group_1")

    @Column(name = "JOB_CLASS_NAME", length = 250, nullable = false)
    private String jobClassName; // 작업을 처리할 클래스 이름 (예: "com.undefinedus.backend.jobs.MyJob")

}
