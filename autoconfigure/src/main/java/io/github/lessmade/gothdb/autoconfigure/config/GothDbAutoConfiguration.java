package io.github.lessmade.gothdb.autoconfigure.config;

import javax.sql.DataSource;

import io.github.lessmade.gothdb.autoconfigure.web.GothDbMetadataController;
import io.github.lessmade.gothdb.autoconfigure.web.GothDbStatusController;
import io.github.lessmade.gothdb.core.service.DatabaseMetadataService;
import io.github.lessmade.gothdb.core.schema.PatternSchemaFilter;
import io.github.lessmade.gothdb.core.schema.SchemaFilter;
import io.github.lessmade.gothdb.core.row.RowQueryOptions;
import io.github.lessmade.gothdb.core.value.DefaultJdbcValueConverter;
import io.github.lessmade.gothdb.core.value.JdbcValueConverter;
import io.github.lessmade.gothdb.exception.GothDbExceptionHandler;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnClass({ DataSource.class, DispatcherServlet.class })
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "gothdb", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(GothDbProperties.class)
public class GothDbAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    JdbcValueConverter gothDbJdbcValueConverter() {
        return new DefaultJdbcValueConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    SchemaFilter gothDbSchemaFilter(GothDbProperties properties) {
        return new PatternSchemaFilter(
                properties.getSchemas().getInclude(),
                properties.getSchemas().getExclude());
    }

    @Bean
    @ConditionalOnMissingBean
    RowQueryOptions gothDbRowQueryOptions(GothDbProperties properties) {
        GothDbProperties.Rows rows = properties.getRows();
        return new RowQueryOptions(rows.getCountMode(), rows.getMaxPageSize(), rows.getQueryTimeout());
    }

    @Bean
    @ConditionalOnMissingBean
    DatabaseMetadataService gothDbDatabaseMetadataService(
            DataSource dataSource,
            JdbcValueConverter valueConverter,
            SchemaFilter schemaFilter,
            RowQueryOptions rowQueryOptions) {
        return new DatabaseMetadataService(dataSource, valueConverter, schemaFilter, rowQueryOptions);
    }

    @Bean
    @ConditionalOnMissingBean
    GothDbStatusController gothDbStatusController(DatabaseMetadataService metadataService) {
        return new GothDbStatusController(metadataService);
    }

    @Bean
    @ConditionalOnMissingBean
    GothDbMetadataController gothDbMetadataController(
            DatabaseMetadataService metadataService, RowQueryOptions rowQueryOptions) {
        return new GothDbMetadataController(metadataService, rowQueryOptions);
    }

    @Bean
    @ConditionalOnMissingBean
    GothDbExceptionHandler gothDbExceptionHandler() {
        return new GothDbExceptionHandler();
    }
}
