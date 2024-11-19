package com.undefinedus.backend.exception.social;

import com.undefinedus.backend.exception.base.BaseException;

public class TabConditionNotEqualException extends BaseException {
    public TabConditionNotEqualException(String message) {
        super(message);
    }
    
    public TabConditionNotEqualException(String message, Throwable cause) {
        super(message, cause);
    }
}
