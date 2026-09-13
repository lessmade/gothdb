package io.github.lessmade.gothdb.autoconfigure.security;

public enum GothDbSecurityMode {

    AUTO,

    BASIC,

    SPRING_SECURITY,

    NONE;

    public GothDbSecurityMode resolve(boolean springSecurityPresent) {
        if (this != AUTO) {
            return this;
        }
        return springSecurityPresent ? SPRING_SECURITY : BASIC;
    }
}
