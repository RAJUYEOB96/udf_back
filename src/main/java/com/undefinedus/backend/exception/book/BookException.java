package com.undefinedus.backend.exception.book;

import com.undefinedus.backend.exception.base.BaseException;

public class BookException extends BaseException {
    public BookException(String message) {
        super(message);
    }
    
    public BookException(String message, Throwable cause) {
        super(message, cause);
    }
}
