package io.github.lessmade.gothdb.autoconfigure.config;

import java.util.ArrayList;
import java.util.List;

import io.github.lessmade.gothdb.core.schema.PatternSchemaFilter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("gothdb")
public class GothDbProperties {

    private boolean enabled = true;

    private String path = "/gothdb";

    private Schemas schemas = new Schemas();

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

    public Schemas getSchemas() {
        return schemas;
    }

    public void setSchemas(Schemas schemas) {
        this.schemas = schemas;
    }

    public static class Schemas {

        private List<String> include = new ArrayList<>();

        private List<String> exclude = new ArrayList<>(PatternSchemaFilter.DEFAULT_EXCLUDES);

        public List<String> getInclude() {
            return include;
        }

        public void setInclude(List<String> include) {
            this.include = include;
        }

        public List<String> getExclude() {
            return exclude;
        }

        public void setExclude(List<String> exclude) {
            this.exclude = exclude;
        }
    }
}
