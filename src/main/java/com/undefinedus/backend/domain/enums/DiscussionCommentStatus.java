package com.undefinedus.backend.domain.enums;

public enum DiscussionCommentStatus {
    ACTIVE , // 평상시
    BLOCKED // 누적 3건으로 자동으로 임시 승인됨 (관리자 확인 필요), 또는 관리자 확인후 처리

}
