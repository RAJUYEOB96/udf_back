package com.undefinedus.backend.exception.handler;

import com.undefinedus.backend.exception.aladinBook.AladinBookNotFoundException;
import com.undefinedus.backend.exception.book.BookDuplicateNotAllowException;
import com.undefinedus.backend.exception.book.BookException;
import com.undefinedus.backend.exception.book.BookExistsException;
import com.undefinedus.backend.exception.book.BookNotFoundException;
import com.undefinedus.backend.exception.book.InvalidStatusException;
import com.undefinedus.backend.exception.bookmark.BookmarkNotFoundException;
import com.undefinedus.backend.exception.discussion.DiscussionException;
import com.undefinedus.backend.exception.discussion.DiscussionNotFoundException;
import com.undefinedus.backend.exception.discussionComment.DiscussionCommentException;
import com.undefinedus.backend.exception.discussionComment.DiscussionCommentNotFoundException;
import com.undefinedus.backend.exception.discussionParticipant.DiscussionParticipantException;
import com.undefinedus.backend.exception.discussionParticipant.DiscussionParticipantNotFoundException;
import com.undefinedus.backend.exception.dto.ErrorResponse;
import com.undefinedus.backend.exception.member.MemberException;
import com.undefinedus.backend.exception.member.MemberNotFoundException;
import com.undefinedus.backend.exception.social.InvalidFollowException;
import com.undefinedus.backend.exception.social.TabConditionNotEqualException;
import com.undefinedus.backend.util.CustomJWTException;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 기존 예외 처리
    @ExceptionHandler(NoSuchElementException.class)
    protected ResponseEntity<?> handleNoSuchElementException(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }

    // @Valid 에서 에러난 후 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<?> handleMethodArgumentNotValidException(
        MethodArgumentNotValidException e) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(CustomJWTException.class)
    protected ResponseEntity<?> handleJWTException(CustomJWTException e) {
        return ResponseEntity.ok()
            .body(Map.of("error", e.getMessage()));
    }

    // 새로 추가된 커스텀 예외 처리
    // Book 관련
    @ExceptionHandler(BookException.class)
    protected ResponseEntity<ErrorResponse> handleBookException(BookException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(BookExistsException.class)
    protected ResponseEntity<ErrorResponse> handleBookExistsException(BookExistsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(BookNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleBookNotFoundException(BookNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(InvalidStatusException.class)
    protected ResponseEntity<ErrorResponse> handleInvalidStatusException(InvalidStatusException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(BookDuplicateNotAllowException.class)
    protected ResponseEntity<ErrorResponse> handleBookDuplicateNotAllowException(
        BookDuplicateNotAllowException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getMessage()));
    }

    // member 관련
    @ExceptionHandler(MemberException.class)
    protected ResponseEntity<ErrorResponse> handleMemberException(MemberException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(MemberNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleMemberNotFoundException(
        MemberNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }

    // bookmark 관련
    @ExceptionHandler(BookmarkNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleBookmarkNotFoundException(
        BookmarkNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }

    // social 관련
    @ExceptionHandler(TabConditionNotEqualException.class)
    protected ResponseEntity<ErrorResponse> handleTabConditionNotEqualException(
        TabConditionNotEqualException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(InvalidFollowException.class)
    protected ResponseEntity<ErrorResponse> handleInvalidFollowException(InvalidFollowException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getMessage()));
    }

    // aladinBook 관련
    @ExceptionHandler(AladinBookNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleAladinBookNotFoundException(
        AladinBookNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }

    // discussion 관련
    @ExceptionHandler(DiscussionException.class)
    protected ResponseEntity<ErrorResponse> handleDiscussionException(DiscussionException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(DiscussionNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleDiscussionNotFoundException(
        DiscussionNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }

    // discussionParticipant 관련
    @ExceptionHandler(DiscussionParticipantException.class)
    protected ResponseEntity<ErrorResponse> handleDiscussionParticipantException(
        DiscussionParticipantException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(DiscussionParticipantNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleDiscussionParticipantNotFoundException(
        DiscussionParticipantNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }

    // 댓글 관련
    @ExceptionHandler(DiscussionCommentException.class)
    protected ResponseEntity<ErrorResponse> handleDiscussionCommentsException(
        DiscussionCommentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(DiscussionCommentNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleDiscussionCommentsNotFoundException(
        DiscussionCommentNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(e.getMessage()));
    }

    // 기타 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("서버 내부 오류가 발생했습니다. : " + e.getMessage()));
    }
}
