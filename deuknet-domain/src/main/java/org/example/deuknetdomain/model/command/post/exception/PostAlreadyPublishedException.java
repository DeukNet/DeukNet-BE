package org.example.deuknetdomain.model.command.post.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 이미 published된 Post를 다시 publish하려고 할 때 발생하는 예외
 */
public class PostAlreadyPublishedException extends DomainException {

    public PostAlreadyPublishedException() {
        super(409, "POST_ALREADY_PUBLISHED", "Post is already published");
    }
}
