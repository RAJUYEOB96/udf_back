package com.undefinedus.backend.exception.discussionParticipant;

import com.undefinedus.backend.exception.base.BaseException;

public class DiscussionParticipantNotFoundException extends BaseException {
    public DiscussionParticipantNotFoundException(String message) {
        super(message);
    }

    public DiscussionParticipantNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
