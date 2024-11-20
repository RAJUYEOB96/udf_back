package com.undefinedus.backend.exception.member;

import com.undefinedus.backend.exception.base.BaseException;

public class MemberNotFoundException extends BaseException {
    public MemberNotFoundException(String message) {
        super(message);
    }
    
    public MemberNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
