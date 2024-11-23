package com.undefinedus.backend.exception.aladinBook;

import com.undefinedus.backend.exception.base.BaseException;

public class AladinBookNotFoundException extends BaseException {
    public AladinBookNotFoundException(String message) {
        super(message);
    }
    
    public AladinBookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
