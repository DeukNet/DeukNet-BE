package org.example.deuknetdomain.domain.auth;

import com.github.f4b6a3.uuid.UuidCreator;
import java.util.UUID;

import lombok.Getter;
import org.example.deuknetdomain.common.seedwork.AggregateRoot;
import org.example.deuknetdomain.common.vo.Email;

@Getter
public class AuthCredential extends AggregateRoot {

    private final UUID userId;
    private final AuthProvider authProvider;
    private final Email email;

    private AuthCredential(UUID id, UUID userId, AuthProvider authProvider, Email email) {
        super(id);
        this.userId = userId;
        this.authProvider = authProvider;
        this.email = email;
    }

    public static AuthCredential create(UUID userId, AuthProvider authProvider, Email email) {
        return new AuthCredential(UuidCreator.getTimeOrderedEpoch(), userId, authProvider, email);
    }

    public static AuthCredential restore(UUID id, UUID userId, AuthProvider authProvider, Email email) {
        return new AuthCredential(id, userId, authProvider, email);
    }
}
