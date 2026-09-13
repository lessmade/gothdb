package io.github.lessmade.gothdb.autoconfigure.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

public final class GothDbRequestMatcher implements RequestMatcher {

    private final String path;

    public GothDbRequestMatcher(String path) {
        this.path = path;
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        String requestPath = request.getRequestURI().substring(request.getContextPath().length());
        return requestPath.equals(path) || requestPath.startsWith(path + "/");
    }
}
