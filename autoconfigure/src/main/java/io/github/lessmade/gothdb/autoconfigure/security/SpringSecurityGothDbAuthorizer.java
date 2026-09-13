package io.github.lessmade.gothdb.autoconfigure.security;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SpringSecurityGothDbAuthorizer implements GothDbAuthorizer {

    private static final String ROLE_PREFIX = "ROLE_";

    private final Set<String> authorities;

    public SpringSecurityGothDbAuthorizer(List<String> roles) {
        this.authorities = roles == null ? Set.of() : roles.stream()
                .filter(role -> role != null && !role.isBlank())
                .map(SpringSecurityGothDbAuthorizer::toAuthority)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public GothDbAuthorization authorize(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticated(authentication)) {
            return GothDbAuthorization.unauthorized(null);
        }
        if (authorities.isEmpty() || hasAnyAuthority(authentication)) {
            return GothDbAuthorization.allow();
        }
        return GothDbAuthorization.forbidden();
    }

    private static boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private boolean hasAnyAuthority(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authorities::contains);
    }

    private static String toAuthority(String role) {
        String trimmed = role.trim();
        return trimmed.startsWith(ROLE_PREFIX) ? trimmed : ROLE_PREFIX + trimmed;
    }
}
