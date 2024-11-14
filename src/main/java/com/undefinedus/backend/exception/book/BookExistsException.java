package com.undefinedus.backend.exception.book;

import com.undefinedus.backend.exception.base.BaseException;

public class BookExistsException extends BaseException {
    public BookExistsException(String message) {
        super(message);
    }
    
    public BookExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
