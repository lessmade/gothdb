package io.github.lessmade.gothdb.core.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import io.github.lessmade.gothdb.core.model.ColumnInfo;
import io.github.lessmade.gothdb.core.model.RowPage;
import io.github.lessmade.gothdb.core.exception.SchemaNotFoundException;
import io.github.lessmade.gothdb.core.value.BinaryValue;
import io.github.lessmade.gothdb.core.value.UnsupportedJdbcValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

@Testcontainers
class PostgreSqlDatabaseMetadataServiceIT {

    @Container
    private static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer("postgres:17.6-alpine")
            .withDatabaseName("gothdb")
            .withUsername("gothdb")
            .withPassword("gothdb");

    private static DatabaseMetadataService metadataService;

    @BeforeAll
    static void setUpDatabase() throws SQLException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setURL(POSTGRESQL.getJdbcUrl());
        dataSource.setUser(POSTGRESQL.getUsername());
        dataSource.setPassword(POSTGRESQL.getPassword());

        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA app");
            statement.execute("""
                    CREATE TABLE app.author (
                        id UUID PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        profile JSONB,
                        aliases TEXT[],
                        created_at TIMESTAMPTZ NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE app.book (
                        author_id UUID NOT NULL,
                        edition INTEGER NOT NULL,
                        title VARCHAR(200) NOT NULL,
                        cover BYTEA,
                        CONSTRAINT pk_book PRIMARY KEY (author_id, edition),
                        CONSTRAINT fk_book_author FOREIGN KEY (author_id) REFERENCES app.author (id)
                    )
                    """);
            statement.execute("CREATE INDEX idx_book_title ON app.book (title)");
            statement.execute("CREATE VIEW app.book_titles AS SELECT author_id, title FROM app.book");
            statement.execute("""
                    INSERT INTO app.author (id, name, profile, aliases, created_at)
                    VALUES ('00000000-0000-0000-0000-000000000001', 'Ursula', '{"active":true}',
                            ARRAY['U. Le Guin'], '2024-01-02T03:04:05Z')
                    """);
            statement.execute("""
                    INSERT INTO app.book (author_id, edition, title, cover) VALUES
                        ('00000000-0000-0000-0000-000000000001', 2, 'The Dispossessed', '\\x0102'),
                        ('00000000-0000-0000-0000-000000000001', 1, 'A Wizard of Earthsea', '\\x0304')
                    """);
        }

        metadataService = new DatabaseMetadataService(dataSource);
    }

    @Test
    void readsPostgreSqlSchemaTablesAndView() {
        assertThat(metadataService.getSchemas())
                .extracting(schema -> schema.name())
                .contains("app")
                .doesNotContain("information_schema", "pg_catalog", "pg_toast")
                .noneMatch(schema -> schema.startsWith("pg_temp_") || schema.startsWith("pg_toast_temp_"));

        assertThat(metadataService.getTables("app"))
                .extracting(table -> table.name(), table -> table.type())
                .contains(
                        tuple("author", "TABLE"),
                        tuple("book", "TABLE"),
                        tuple("book_titles", "VIEW"));

        assertThatThrownBy(() -> metadataService.getTables("pg_catalog"))
                .isInstanceOf(SchemaNotFoundException.class);
    }

    @Test
    void readsPostgreSqlColumnTypes() {
        assertThat(metadataService.getColumns("app", "author"))
                .extracting(ColumnInfo::name, ColumnInfo::jdbcType, ColumnInfo::typeName)
                .contains(
                        tuple("id", Types.OTHER, "uuid"),
                        tuple("profile", Types.OTHER, "jsonb"),
                        tuple("aliases", Types.ARRAY, "_text"),
                        tuple("created_at", Types.TIMESTAMP, "timestamptz"));
    }

    @Test
    void readsPostgreSqlKeysAndIndexes() {
        assertThat(metadataService.getPrimaryKeys("app", "book"))
                .extracting(key -> key.columnName(), key -> key.keySequence())
                .containsExactlyInAnyOrder(tuple("author_id", 1), tuple("edition", 2));

        assertThat(metadataService.getForeignKeys("app", "book"))
                .extracting(key -> key.name(), key -> key.columnName(), key -> key.referencedTable())
                .containsExactly(tuple("fk_book_author", "author_id", "author"));

        assertThat(metadataService.getIndexes("app", "book"))
                .extracting(index -> index.name(), index -> index.columnName())
                .contains(tuple("idx_book_title", "title"));
    }

    @Test
    void readsRowsInCompositePrimaryKeyOrder() {
        RowPage page = metadataService.getRows("app", "book", 0, 10);

        assertThat(page.totalElements()).isEqualTo(2);
        assertThat(page.rows())
                .extracting(row -> row.get("edition"), row -> row.get("title"))
                .containsExactly(
                        tuple(1, "A Wizard of Earthsea"),
                        tuple(2, "The Dispossessed"));
    }

    @Test
    void convertsPostgreSqlValuesToTransportSafeValues() {
        RowPage authors = metadataService.getRows("app", "author", 0, 10);
        assertThat(authors.rows()).hasSize(1);

        Object profile = authors.rows().get(0).get("profile");
        assertThat(profile).isInstanceOf(UnsupportedJdbcValue.class);
        UnsupportedJdbcValue unsupportedProfile = (UnsupportedJdbcValue) profile;
        assertThat(unsupportedProfile.type()).isEqualTo("org.postgresql.util.PGobject");
        assertThat(unsupportedProfile.value()).contains("\"active\"").contains("true");
        assertThat(authors.rows().get(0).get("aliases")).isEqualTo(List.of("U. Le Guin"));
        assertThat(authors.rows().get(0).get("created_at"))
                .isInstanceOfAny(java.util.Date.class, TemporalAccessor.class);

        RowPage books = metadataService.getRows("app", "book", 0, 1);
        assertThat(books.rows().get(0).get("cover"))
                .isEqualTo(new BinaryValue("base64", "AwQ=", false, 2));
    }
}
