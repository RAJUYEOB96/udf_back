package com.undefinedus.backend.exception.report;

import com.undefinedus.backend.exception.base.BaseException;

public class ReportNotFoundException extends BaseException {
    public ReportNotFoundException(String message) {
        super(message);
    }

    public ReportNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
