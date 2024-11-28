package com.undefinedus.backend.exception.calendar;

import com.undefinedus.backend.exception.base.BaseException;

public class YearOrMonthException extends BaseException {
    public YearOrMonthException(String message) {
        super(message);
    }
    
    public YearOrMonthException(String message, Throwable cause) {
        super(message, cause);
    }
}
