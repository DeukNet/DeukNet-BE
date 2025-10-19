package org.example.deuknetdomain.model.command.post.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * Draft 상태가 아닌 Post를 publish하려고 할 때 발생하는 예외
 */
public class CannotPublishNonDraftPostException extends DomainException {

    public CannotPublishNonDraftPostException() {
        super(400, "CANNOT_PUBLISH_NON_DRAFT", "Only draft posts can be published");
    }
}
