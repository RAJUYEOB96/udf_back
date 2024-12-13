package com.undefinedus.backend.scheduler.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "QRTZ_TRIGGERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuartzTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id; // 고유 식별자로 사용되는 ID

    @Column(name = "SCHED_NAME", length = 120, nullable = false)
    private String schedName; // 스케줄러의 이름 (예: "DEFAULT")

    @Column(name = "TRIGGER_NAME", length = 200, nullable = false)
    private String triggerName; // 트리거의 이름 (예: "trigger_1")

    @Column(name = "TRIGGER_GROUP", length = 200, nullable = false)
    private String triggerGroup; // 트리거가 속한 그룹 (예: "group_1")

    @Column(name = "TRIGGER_TYPE", length = 8, nullable = false)
    private String triggerType; // 트리거의 타입 (예: "CRON", "SIMPLE")

    @Column(name = "START_TIME", nullable = false)
    private Long startTime; // 트리거의 시작 시간 (밀리초 단위)

    @Column
    private Date startTimeEasy;
    
    public void updateStartTimeEasy(Date startTimeEasy) {
        this.startTime = startTimeEasy.getTime();
        this.startTimeEasy = startTimeEasy;
    }
}
