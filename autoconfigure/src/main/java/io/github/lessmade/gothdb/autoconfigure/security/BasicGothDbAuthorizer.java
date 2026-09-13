package io.github.lessmade.gothdb.autoconfigure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;

public final class BasicGothDbAuthorizer implements GothDbAuthorizer {

    private static final String SCHEME = "Basic ";

    private final byte[] expectedCredentials;
    private final String challenge;

    public BasicGothDbAuthorizer(String username, String password, String realm) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("gothdb.security.username must not be blank");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("gothdb.security.password must not be empty");
        }
        this.expectedCredentials = (username + ":" + password).getBytes(StandardCharsets.UTF_8);
        this.challenge = "Basic realm=\"" + sanitizeRealm(realm) + "\", charset=\"UTF-8\"";
    }

    @Override
    public GothDbAuthorization authorize(HttpServletRequest request) {
        byte[] presented = presentedCredentials(request);
        if (presented == null || !MessageDigest.isEqual(expectedCredentials, presented)) {
            return GothDbAuthorization.unauthorized(challenge);
        }
        return GothDbAuthorization.allow();
    }

    private static byte[] presentedCredentials(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.regionMatches(true, 0, SCHEME, 0, SCHEME.length())) {
            return null;
        }
        try {
            return Base64.getDecoder().decode(header.substring(SCHEME.length()).trim());
        }
        catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String sanitizeRealm(String realm) {
        if (realm == null || realm.isBlank()) {
            return "GothDB";
        }
        return realm.replaceAll("[\"\\\\\\r\\n]", "");
    }
}
