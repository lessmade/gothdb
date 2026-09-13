package io.github.lessmade.gothdb.autoconfigure.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public final class GothDbAccessFilter extends OncePerRequestFilter {

    private final GothDbAuthorizer authorizer;

    public GothDbAccessFilter(GothDbAuthorizer authorizer) {
        this.authorizer = authorizer;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        GothDbAuthorization authorization = authorizer.authorize(request);
        if (authorization.granted()) {
            chain.doFilter(request, response);
            return;
        }
        deny(response, authorization);
    }

    private static void deny(HttpServletResponse response, GothDbAuthorization authorization) throws IOException {
        HttpStatus status = HttpStatus.valueOf(authorization.status());
        response.setStatus(status.value());
        if (authorization.challenge() != null) {
            response.setHeader("WWW-Authenticate", authorization.challenge());
        }
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        byte[] body = body(status);
        response.setContentLength(body.length);
        response.getOutputStream().write(body);
    }

    private static byte[] body(HttpStatus status) {
        String message = status == HttpStatus.UNAUTHORIZED ? "Authentication required" : "Access denied";
        String json = "{\"status\":" + status.value()
                + ",\"error\":\"" + status.getReasonPhrase() + "\""
                + ",\"message\":\"" + message + "\"}";
        return json.getBytes(StandardCharsets.UTF_8);
    }
}
