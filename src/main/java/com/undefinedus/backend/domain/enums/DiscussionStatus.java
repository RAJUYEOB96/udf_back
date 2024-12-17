package com.undefinedus.backend.domain.enums;

public enum DiscussionStatus {
    PROPOSED("발의됨"),        // 토론 시작 전 (발의)
    SCHEDULED("대기중"),        // 토론 시작 전 (대기)
    IN_PROGRESS("진행중"),    // 토론 진행 중 (ONGOING보다 더 일반적인 표현)
    ANALYZING("분석중"),      // AI 분석 중
    COMPLETED("종료됨");

    private final String description;
    
    DiscussionStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}