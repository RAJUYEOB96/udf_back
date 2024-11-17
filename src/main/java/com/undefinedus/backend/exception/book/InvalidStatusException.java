package com.undefinedus.backend.exception.book;

import com.undefinedus.backend.exception.base.BaseException;

public class InvalidStatusException extends BaseException {
    public InvalidStatusException(String message) {
        super(message);
    }
    
    public InvalidStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}
