package io.github.lessmade.gothdb.autoconfigure;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import io.github.lessmade.gothdb.autoconfigure.web.GothDbExceptionHandler;
import io.github.lessmade.gothdb.autoconfigure.web.GothDbMetadataController;
import io.github.lessmade.gothdb.autoconfigure.web.GothDbStatus;
import io.github.lessmade.gothdb.autoconfigure.web.GothDbStatusController;
import io.github.lessmade.gothdb.core.service.DatabaseMetadataService;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GothDbAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GothDbAutoConfiguration.class));

    @Test
    void backsOffWithoutDataSource() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(GothDbStatusController.class);
            assertThat(context).doesNotHaveBean(GothDbMetadataController.class);
            assertThat(context).doesNotHaveBean(DatabaseMetadataService.class);
        });
    }

    @Test
    void configuresGothDbWhenDataSourceIsAvailable() {
        contextRunner
                .withBean(DataSource.class, GothDbAutoConfigurationTests::dataSource)
                .run(context -> {
                    assertThat(context).hasSingleBean(GothDbStatusController.class);
                    assertThat(context).hasSingleBean(GothDbMetadataController.class);
                    assertThat(context).hasSingleBean(DatabaseMetadataService.class);
                    assertThat(context).hasSingleBean(GothDbProperties.class);
                });
    }

    @Test
    void configuresAfterBootCreatesDataSource() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        DataSourceAutoConfiguration.class,
                        GothDbAutoConfiguration.class))
                .withPropertyValues("spring.datasource.url=jdbc:h2:mem:auto-configured")
                .run(context -> {
                    assertThat(context).hasSingleBean(DataSource.class);
                    assertThat(context).hasSingleBean(GothDbStatusController.class);
                    assertThat(context).hasSingleBean(GothDbMetadataController.class);
                    assertThat(context).hasSingleBean(DatabaseMetadataService.class);
                });
    }

    @Test
    void canBeDisabled() {
        contextRunner
                .withBean(DataSource.class, GothDbAutoConfigurationTests::dataSource)
                .withPropertyValues("gothdb.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(GothDbStatusController.class);
                    assertThat(context).doesNotHaveBean(GothDbMetadataController.class);
                    assertThat(context).doesNotHaveBean(DatabaseMetadataService.class);
                });
    }

    @Test
    void bindsCustomBasePath() {
        contextRunner
                .withBean(DataSource.class, GothDbAutoConfigurationTests::dataSource)
                .withPropertyValues("gothdb.path=/database")
                .run(context -> assertThat(context.getBean(GothDbProperties.class).getPath())
                        .isEqualTo("/database"));
    }

    @Test
    void statusContainsNonSensitiveDatabaseMetadata() {
        contextRunner
                .withBean(DataSource.class, GothDbAutoConfigurationTests::dataSource)
                .run(context -> {
                    GothDbStatus status = context.getBean(GothDbStatusController.class).status();

                    assertThat(status.status()).isEqualTo("UP");
                    assertThat(status.database()).isEqualTo("H2");
                    assertThat(status.databaseVersion()).isNotBlank();
                    assertThat(status.driver()).isNotBlank();
                });
    }

    @Test
    void exposesStatusAtConfiguredBasePath() {
        contextRunner
                .withBean(DataSource.class, GothDbAutoConfigurationTests::dataSource)
                .withPropertyValues("gothdb.path=/database")
                .run(context -> {
                    MockMvc mockMvc = MockMvcBuilders
                            .standaloneSetup(
                                    context.getBean(GothDbStatusController.class),
                                    context.getBean(GothDbMetadataController.class))
                            .setControllerAdvice(context.getBean(GothDbExceptionHandler.class))
                            .addPlaceholderValue("gothdb.path", "/database")
                            .build();

                    mockMvc.perform(get("/database/api/status"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.status").value("UP"))
                            .andExpect(jsonPath("$.database").value("H2"))
                            .andExpect(jsonPath("$.url").doesNotExist())
                            .andExpect(jsonPath("$.username").doesNotExist())
                            .andExpect(jsonPath("$.password").doesNotExist());

                    mockMvc.perform(get("/database/api/schemas"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$[?(@.name == 'PUBLIC')]").exists());

                    mockMvc.perform(get("/database/api/schemas/PUBLIC/tables"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$[?(@.name == 'BOOK')]").exists());

                    mockMvc.perform(get("/database/api/schemas/PUBLIC/tables/BOOK/columns"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$[0].name").value("ID"))
                            .andExpect(jsonPath("$[1].name").value("TITLE"));

                    mockMvc.perform(get("/database/api/schemas/PUBLIC/tables/BOOK/primary-key"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$[0].columnName").value("ID"));

                    mockMvc.perform(get("/database/api/schemas/PUBLIC/tables/BOOK/foreign-keys"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$").isArray())
                            .andExpect(jsonPath("$").isEmpty());

                    mockMvc.perform(get("/database/api/schemas/PUBLIC/tables/BOOK/indexes"))
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$").isArray());

                    mockMvc.perform(get("/database/api/schemas/MISSING/tables"))
                            .andExpect(status().isNotFound())
                            .andExpect(jsonPath("$.status").value(404))
                            .andExpect(jsonPath("$.message").value("Schema not found: MISSING"));

                    mockMvc.perform(get("/database/api/schemas/PUBLIC/tables/MISSING/columns"))
                            .andExpect(status().isNotFound())
                            .andExpect(jsonPath("$.message").value("Table not found: PUBLIC.MISSING"));

                    mockMvc.perform(get("/database/api/schemas/{schema}/tables", " "))
                            .andExpect(status().isBadRequest())
                            .andExpect(jsonPath("$.status").value(400));
                });
    }

    private static DataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:gothdb;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS BOOK (ID BIGINT PRIMARY KEY, TITLE VARCHAR(100))");
        }
        catch (SQLException exception) {
            throw new IllegalStateException("Failed to prepare test database", exception);
        }
        return dataSource;
    }
}
