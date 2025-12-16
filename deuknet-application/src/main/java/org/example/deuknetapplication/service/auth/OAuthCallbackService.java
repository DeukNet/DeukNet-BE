package org.example.deuknetapplication.service.auth;

import org.example.deuknetapplication.port.in.auth.OAuthCallbackUseCase;
import org.example.deuknetapplication.port.out.external.OAuthPort;
import org.example.deuknetapplication.port.out.external.OAuthStateManagerPort;
import org.example.deuknetapplication.port.out.repository.AuthCredentialRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.JwtPort;
import org.example.deuknetdomain.common.vo.Email;
import org.example.deuknetdomain.domain.auth.AuthCredential;
import org.example.deuknetdomain.domain.auth.AuthProvider;
import org.example.deuknetdomain.domain.auth.OAuthUserInfo;
import org.example.deuknetdomain.domain.auth.TokenPair;
import org.example.deuknetdomain.domain.user.User;
import org.example.deuknetdomain.domain.user.exception.UserNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class OAuthCallbackService implements OAuthCallbackUseCase {

    private final OAuthPort oAuthPort;
    private final OAuthStateManagerPort oAuthStateManagerPort;
    private final AuthCredentialRepository authCredentialRepository;
    private final UserRepository userRepository;
    private final JwtPort jwtPort;

    public OAuthCallbackService(
            OAuthPort oAuthPort,
            OAuthStateManagerPort oAuthStateManagerPort,
            AuthCredentialRepository authCredentialRepository,
            UserRepository userRepository,
            JwtPort jwtPort
    ) {
        this.oAuthPort = oAuthPort;
        this.oAuthStateManagerPort = oAuthStateManagerPort;
        this.authCredentialRepository = authCredentialRepository;
        this.userRepository = userRepository;
        this.jwtPort = jwtPort;
    }

    @Override
    public TokenPair handleCallback(String code, String state, AuthProvider provider) {
        // Validate state token for CSRF protection
        oAuthStateManagerPort.validateState(state);

        // Get user info from OAuth provider
        OAuthUserInfo oAuthUserInfo = oAuthPort.getUserInfo(code, provider);

        Email email = Email.from(oAuthUserInfo.email());

        // Find or create user
        AuthCredential authCredential = authCredentialRepository
                .findByEmailAndProvider(email, provider)
                .orElseGet(() -> createNewUser(oAuthUserInfo, email));

        User user = userRepository.findByAuthCredentialId(authCredential.getId())
                .orElseThrow(UserNotFoundException::new);

        return jwtPort.createTokenPair(user.getId());
    }

    private AuthCredential createNewUser(OAuthUserInfo oAuthUserInfo, Email email) {
        UUID tempUserId = UUID.randomUUID();
        AuthCredential authCredential = AuthCredential.create(
                tempUserId,
                oAuthUserInfo.provider(),
                email
        );
        authCredential = authCredentialRepository.save(authCredential);

        String username = generateUniqueUsername(oAuthUserInfo.name());
        User user = User.create(
                authCredential.getId(),
                username,
                oAuthUserInfo.name(),
                null,
                oAuthUserInfo.picture()
        );
        userRepository.save(user);

        return authCredential;
    }

    private String generateUniqueUsername(String baseName) {
        String username = baseName.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        if (username.isEmpty()) {
            username = "user";
        }

        String finalUsername = username;
        int suffix = 1;
        while (userRepository.findByUsername(finalUsername).isPresent()) {
            finalUsername = username + suffix;
            suffix++;
        }

        return finalUsername;
    }
}
