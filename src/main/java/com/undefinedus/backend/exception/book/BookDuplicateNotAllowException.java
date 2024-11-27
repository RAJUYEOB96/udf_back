package com.undefinedus.backend.exception.book;

import com.undefinedus.backend.exception.base.BaseException;

public class BookDuplicateNotAllowException extends BaseException {
    
    public BookDuplicateNotAllowException(String message) {
        super(message);
    }
    
    public BookDuplicateNotAllowException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
