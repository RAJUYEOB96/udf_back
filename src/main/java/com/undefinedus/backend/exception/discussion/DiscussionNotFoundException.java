package com.undefinedus.backend.exception.discussion;

import com.undefinedus.backend.exception.base.BaseException;

public class DiscussionNotFoundException extends BaseException {
    public DiscussionNotFoundException(String message) {
        super(message);
    }

    public DiscussionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
