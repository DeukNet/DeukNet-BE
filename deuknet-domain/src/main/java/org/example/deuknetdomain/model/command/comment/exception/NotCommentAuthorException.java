package org.example.deuknetdomain.model.command.comment.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * Comment의 작성자가 아닌 사용자가 수정하려고 할 때 발생하는 예외
 */
public class NotCommentAuthorException extends DomainException {

    public NotCommentAuthorException() {
        super(403, "NOT_COMMENT_AUTHOR", "Only the author can modify this comment");
    }
}
