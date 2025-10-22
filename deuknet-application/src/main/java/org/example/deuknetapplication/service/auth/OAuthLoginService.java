package org.example.deuknetapplication.service.auth;

import org.example.deuknetapplication.port.in.auth.OAuthLoginUseCase;
import org.example.deuknetapplication.port.out.external.OAuthPort;
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
public class OAuthLoginService implements OAuthLoginUseCase {

    private final OAuthPort oAuthPort;
    private final AuthCredentialRepository authCredentialRepository;
    private final UserRepository userRepository;
    private final JwtPort jwtPort;

    public OAuthLoginService(
            OAuthPort oAuthPort,
            AuthCredentialRepository authCredentialRepository,
            UserRepository userRepository,
            JwtPort jwtPort
    ) {
        this.oAuthPort = oAuthPort;
        this.authCredentialRepository = authCredentialRepository;
        this.userRepository = userRepository;
        this.jwtPort = jwtPort;
    }

    @Override
    public TokenPair login(String authorizationCode, AuthProvider provider) {
        // 1. OAuth Provider로부터 사용자 정보 가져오기
        OAuthUserInfo oAuthUserInfo = oAuthPort.getUserInfo(authorizationCode, provider);
        
        Email email = Email.from(oAuthUserInfo.getEmail());
        
        // 2. AuthCredential 조회 또는 생성
        AuthCredential authCredential = authCredentialRepository
                .findByEmailAndProvider(email, provider)
                .orElseGet(() -> createNewUser(oAuthUserInfo, email));
        
        // 3. User 조회
        User user = userRepository.findByAuthCredentialId(authCredential.getId())
                .orElseThrow(UserNotFoundException::new);
        
        // 4. JWT 토큰 생성
        String accessToken = jwtPort.generateAccessToken(user.getId());
        String refreshToken = jwtPort.generateRefreshToken(user.getId());
        
        return new TokenPair(accessToken, refreshToken);
    }

    private AuthCredential createNewUser(OAuthUserInfo oAuthUserInfo, Email email) {
        // 임시 userId로 AuthCredential 먼저 생성
        UUID tempUserId = UUID.randomUUID();
        AuthCredential authCredential = AuthCredential.create(
                tempUserId,
                oAuthUserInfo.getProvider(),
                email
        );
        authCredential = authCredentialRepository.save(authCredential);
        
        // User 생성
        String username = generateUniqueUsername(oAuthUserInfo.getName());
        User user = User.create(
                authCredential.getId(),
                username,
                oAuthUserInfo.getName(),
                null,
                oAuthUserInfo.getPicture()
        );
        user = userRepository.save(user);
        
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
