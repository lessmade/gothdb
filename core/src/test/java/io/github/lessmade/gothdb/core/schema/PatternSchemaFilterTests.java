package io.github.lessmade.gothdb.core.schema;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PatternSchemaFilterTests {

    @Test
    void hidesPostgreSqlSystemSchemasByDefault() {
        SchemaFilter filter = PatternSchemaFilter.defaults();

        assertThat(filter.isVisible("app")).isTrue();
        assertThat(filter.isVisible("information_schema")).isFalse();
        assertThat(filter.isVisible("pg_catalog")).isFalse();
        assertThat(filter.isVisible("pg_toast")).isFalse();
        assertThat(filter.isVisible("pg_temp_3")).isFalse();
        assertThat(filter.isVisible("pg_toast_temp_3")).isFalse();
    }

    @Test
    void appliesIncludesBeforeExcludes() {
        SchemaFilter filter = new PatternSchemaFilter(
                List.of("app_*"),
                List.of("*_private"));

        assertThat(filter.isVisible("app_public")).isTrue();
        assertThat(filter.isVisible("app_private")).isFalse();
        assertThat(filter.isVisible("other")).isFalse();
    }

    @Test
    void treatsRegexCharactersAsLiteralGlobCharacters() {
        SchemaFilter filter = new PatternSchemaFilter(List.of("tenant.v1"), List.of());

        assertThat(filter.isVisible("tenant.v1")).isTrue();
        assertThat(filter.isVisible("tenantXv1")).isFalse();
    }
}
