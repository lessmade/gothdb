package io.github.lessmade.gothdb.autoconfigure;

import javax.sql.DataSource;

import io.github.lessmade.gothdb.autoconfigure.web.GothDbExceptionHandler;
import io.github.lessmade.gothdb.autoconfigure.web.GothDbMetadataController;
import io.github.lessmade.gothdb.autoconfigure.web.GothDbStatusController;
import io.github.lessmade.gothdb.core.service.DatabaseMetadataService;

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
    DatabaseMetadataService gothDbDatabaseMetadataService(DataSource dataSource) {
        return new DatabaseMetadataService(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    GothDbStatusController gothDbStatusController(DatabaseMetadataService metadataService) {
        return new GothDbStatusController(metadataService);
    }

    @Bean
    @ConditionalOnMissingBean
    GothDbMetadataController gothDbMetadataController(DatabaseMetadataService metadataService) {
        return new GothDbMetadataController(metadataService);
    }

    @Bean
    @ConditionalOnMissingBean
    GothDbExceptionHandler gothDbExceptionHandler() {
        return new GothDbExceptionHandler();
    }
}
