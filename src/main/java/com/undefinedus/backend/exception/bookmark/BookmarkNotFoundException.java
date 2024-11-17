package com.undefinedus.backend.exception.bookmark;

import com.undefinedus.backend.exception.base.BaseException;

public class BookmarkNotFoundException extends BaseException {
    public BookmarkNotFoundException(String message) {
        super(message);
    }
    
    public BookmarkNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
