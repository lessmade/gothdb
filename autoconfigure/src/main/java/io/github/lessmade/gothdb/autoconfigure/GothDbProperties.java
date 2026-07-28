package io.github.lessmade.gothdb.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("gothdb")
public class GothDbProperties {

    /**
     * Whether GothDB is enabled.
     */
    private boolean enabled = true;

    /**
     * Base path of the GothDB web interface.
     */
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
