package io.github.lessmade.gothdb.autoconfigure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("gothdb")
public class GothDbProperties {

    private boolean enabled = true;

    private String path = "/gothdb";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
