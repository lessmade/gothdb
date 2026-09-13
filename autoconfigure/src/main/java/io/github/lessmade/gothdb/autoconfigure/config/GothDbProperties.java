package io.github.lessmade.gothdb.autoconfigure.config;

import java.util.ArrayList;
import java.util.List;
import java.time.Duration;

import io.github.lessmade.gothdb.autoconfigure.security.GothDbSecurityMode;
import io.github.lessmade.gothdb.core.row.CountMode;
import io.github.lessmade.gothdb.core.schema.PatternSchemaFilter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("gothdb")
public class GothDbProperties {

    private boolean enabled = true;

    private String path = "/gothdb";

    private Schemas schemas = new Schemas();

    private Rows rows = new Rows();

    private Ui ui = new Ui();

    private Security security = new Security();

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

    public Rows getRows() {
        return rows;
    }

    public void setRows(Rows rows) {
        this.rows = rows;
    }

    public Ui getUi() {
        return ui;
    }

    public void setUi(Ui ui) {
        this.ui = ui;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
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

    public static class Rows {

        private CountMode countMode = CountMode.EXACT;

        private int maxPageSize = 200;

        private Duration queryTimeout = Duration.ofSeconds(5);

        public CountMode getCountMode() {
            return countMode;
        }

        public void setCountMode(CountMode countMode) {
            this.countMode = countMode;
        }

        public int getMaxPageSize() {
            return maxPageSize;
        }

        public void setMaxPageSize(int maxPageSize) {
            this.maxPageSize = maxPageSize;
        }

        public Duration getQueryTimeout() {
            return queryTimeout;
        }

        public void setQueryTimeout(Duration queryTimeout) {
            this.queryTimeout = queryTimeout;
        }
    }

    public static class Ui {

        private boolean enabled = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Security {

        private GothDbSecurityMode mode = GothDbSecurityMode.AUTO;

        private String username = "gothdb";

        private String password;

        private String realm = "GothDB";

        private List<String> roles = new ArrayList<>();

        public GothDbSecurityMode getMode() {
            return mode;
        }

        public void setMode(GothDbSecurityMode mode) {
            this.mode = mode;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRealm() {
            return realm;
        }

        public void setRealm(String realm) {
            this.realm = realm;
        }

        public List<String> getRoles() {
            return roles;
        }

        public void setRoles(List<String> roles) {
            this.roles = roles;
        }
    }
}
