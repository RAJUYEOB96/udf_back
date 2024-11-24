package com.undefinedus.backend.exception.discussionComment;

import com.undefinedus.backend.exception.base.BaseException;

public class DiscussionCommentNotFoundException extends BaseException {
    public DiscussionCommentNotFoundException(String message) {
        super(message);
    }

    public DiscussionCommentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
