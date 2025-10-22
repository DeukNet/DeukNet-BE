package org.example.deuknetdomain.domain.post.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * Post의 작성자가 아닌 사용자가 수정하려고 할 때 발생하는 예외
 */
public class NotPostAuthorException extends DomainException {

    public NotPostAuthorException() {
        super(403, "NOT_POST_AUTHOR", "Only the author can modify this post");
    }
}
