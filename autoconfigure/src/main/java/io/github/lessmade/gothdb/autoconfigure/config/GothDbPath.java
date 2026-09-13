package io.github.lessmade.gothdb.autoconfigure.config;

public final class GothDbPath {

    private GothDbPath() {
    }

    public static String normalize(String path) {
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
