package org.example.deuknetdomain.model.command.comment.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * Comment가 최대 길이를 초과할 때 발생하는 예외
 */
public class CommentTooLongException extends DomainException {

    public CommentTooLongException() {
        super(400, "COMMENT_TOO_LONG", "Comment exceeds maximum length");
    }
}
