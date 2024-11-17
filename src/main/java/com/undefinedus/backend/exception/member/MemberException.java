package com.undefinedus.backend.exception.member;

import com.undefinedus.backend.exception.base.BaseException;

public class MemberException extends BaseException {
    public MemberException(String message) {
        super(message);
    }
    
    public MemberException(String message, Throwable cause) {
        super(message, cause);
    }
}
