package io.github.lessmade.gothdb.autoconfigure.security;

public record GothDbAuthorization(boolean granted, int status, String challenge) {

    private static final GothDbAuthorization ALLOWED = new GothDbAuthorization(true, 200, null);

    public static GothDbAuthorization allow() {
        return ALLOWED;
    }

    public static GothDbAuthorization unauthorized(String challenge) {
        return new GothDbAuthorization(false, 401, challenge);
    }

    public static GothDbAuthorization forbidden() {
        return new GothDbAuthorization(false, 403, null);
    }
}
