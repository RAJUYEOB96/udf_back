package com.undefinedus.backend.exception.book;

import com.undefinedus.backend.exception.base.BaseException;

public class BookNotFoundException extends BaseException {
    public BookNotFoundException(String message) {
        super(message);
    }
    
    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
