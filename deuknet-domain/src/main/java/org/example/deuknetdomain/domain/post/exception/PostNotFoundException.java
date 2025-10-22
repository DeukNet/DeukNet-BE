package org.example.deuknetdomain.domain.post.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * Post를 찾을 수 없을 때 발생하는 예외
 */
public class PostNotFoundException extends DomainException {

    public PostNotFoundException() {
        super(404, "POST_NOT_FOUND", "Post not found");
    }
}
