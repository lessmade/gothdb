package io.github.lessmade.gothdb.autoconfigure.security;

import jakarta.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface GothDbAuthorizer {

    GothDbAuthorization authorize(HttpServletRequest request);

    static GothDbAuthorizer permitAll() {
        return request -> GothDbAuthorization.allow();
    }
}
