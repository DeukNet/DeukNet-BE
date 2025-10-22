package org.example.deuknetdomain.domain.comment.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * Comment를 찾을 수 없을 때 발생하는 예외
 */
public class CommentNotFoundException extends DomainException {

    public CommentNotFoundException() {
        super(404, "COMMENT_NOT_FOUND", "Comment not found");
    }
}
