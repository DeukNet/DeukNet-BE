package org.example.deuknetdomain.domain.reaction.exception;

import org.example.deuknetdomain.common.exception.DomainException;

/**
 * 이미 동일한 대상에 Reaction이 존재할 때 발생하는 예외
 */
public class DuplicateReactionException extends DomainException {

    public DuplicateReactionException() {
        super(409, "DUPLICATE_REACTION", "User has already reacted to this target");
    }
}
