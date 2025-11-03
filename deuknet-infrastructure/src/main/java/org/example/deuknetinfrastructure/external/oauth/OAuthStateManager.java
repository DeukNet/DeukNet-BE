package org.example.deuknetinfrastructure.external.oauth;

import org.example.deuknetapplication.port.out.external.OAuthStateManagerPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class OAuthStateManager implements OAuthStateManagerPort {

    private final Map<String, Long> stateTokens = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private static final long STATE_EXPIRATION_MS = TimeUnit.MINUTES.toMillis(10);

    @Override
    public String generateState() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        stateTokens.put(state, System.currentTimeMillis());
        cleanupExpiredStates();

        return state;
    }

    @Override
    public void validateState(String state) {
        if (state == null || !stateTokens.containsKey(state)) {
            throw new IllegalStateException("Invalid or missing state parameter");
        }

        Long timestamp = stateTokens.remove(state);
        if (System.currentTimeMillis() - timestamp > STATE_EXPIRATION_MS) {
            throw new IllegalStateException("State parameter has expired");
        }
    }

    private void cleanupExpiredStates() {
        long now = System.currentTimeMillis();
        stateTokens.entrySet().removeIf(entry ->
            now - entry.getValue() > STATE_EXPIRATION_MS
        );
    }
}
