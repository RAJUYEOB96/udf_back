package com.undefinedus.backend.domain.enums;

public enum ReportStatus {
    PENDING,      // 접수됨
    TEMPORARY_ACCEPTED, // 누적 3건으로 자동으로 임시 승인됨 (관리자 확인 필요)
    ACCEPTED,     // 승인됨 (제재 필요)
    REJECTED,     // 거절됨 (신고 내용 부적절)
}
