package com.undefinedus.backend.exception.social;

import com.undefinedus.backend.exception.base.BaseException;

public class InvalidFollowException extends BaseException {
    public InvalidFollowException(String message) {
        super(message);
    }
    
    public InvalidFollowException(String message, Throwable cause) {
        super(message, cause);
    }
}
