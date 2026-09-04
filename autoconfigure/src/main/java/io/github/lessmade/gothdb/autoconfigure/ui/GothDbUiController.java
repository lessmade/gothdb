package io.github.lessmade.gothdb.autoconfigure.ui;

import java.net.URI;

import io.github.lessmade.gothdb.autoconfigure.config.GothDbProperties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GothDbUiController {

    private static final Resource INDEX = new ClassPathResource("META-INF/gothdb/index.html");

    private final String path;

    public GothDbUiController(GothDbProperties properties) {
        this.path = normalizePath(properties.getPath());
    }

    @GetMapping("${gothdb.path:/gothdb}")
    public ResponseEntity<Void> redirectToTrailingSlash() {
        return ResponseEntity.status(302).location(URI.create(path + "/")).build();
    }

    @GetMapping(value = "${gothdb.path:/gothdb}/", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<Resource> index() {
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(INDEX);
    }

    static String normalizePath(String path) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            throw new IllegalArgumentException("gothdb.path must be a non-root path");
        }
        String normalized = path.startsWith("/") ? path : "/" + path;
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
