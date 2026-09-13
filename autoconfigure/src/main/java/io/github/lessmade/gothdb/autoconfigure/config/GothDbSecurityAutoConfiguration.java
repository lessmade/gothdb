package io.github.lessmade.gothdb.autoconfigure.config;

import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import io.github.lessmade.gothdb.autoconfigure.security.BasicGothDbAuthorizer;
import io.github.lessmade.gothdb.autoconfigure.security.GothDbAccessFilter;
import io.github.lessmade.gothdb.autoconfigure.security.GothDbAuthorizer;
import io.github.lessmade.gothdb.autoconfigure.security.GothDbRequestMatcher;
import io.github.lessmade.gothdb.autoconfigure.security.GothDbSecurityHeadersFilter;
import io.github.lessmade.gothdb.autoconfigure.security.GothDbSecurityMode;
import io.github.lessmade.gothdb.autoconfigure.security.SpringSecurityGothDbAuthorizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.util.ClassUtils;
import org.springframework.web.servlet.DispatcherServlet;

@AutoConfiguration(after = DataSourceAutoConfiguration.class)
@ConditionalOnClass({ DataSource.class, DispatcherServlet.class })
@ConditionalOnBean(DataSource.class)
@ConditionalOnProperty(prefix = "gothdb", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(GothDbProperties.class)
public class GothDbSecurityAutoConfiguration {

    private static final String SPRING_SECURITY_MARKER = "org.springframework.security.core.context.SecurityContextHolder";

    private static final int SPRING_SECURITY_FILTER_ORDER = -100;

    private static final int HEADERS_FILTER_ORDER = SPRING_SECURITY_FILTER_ORDER + 1;

    private static final int ACCESS_FILTER_ORDER = SPRING_SECURITY_FILTER_ORDER + 2;

    private static final Log logger = LogFactory.getLog(GothDbSecurityAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    GothDbAuthorizer gothDbAuthorizer(GothDbProperties properties, ResourceLoader resourceLoader) {
        GothDbProperties.Security security = properties.getSecurity();
        boolean springSecurityPresent = isSpringSecurityPresent(resourceLoader.getClassLoader());
        GothDbSecurityMode mode = security.getMode().resolve(springSecurityPresent);
        String path = GothDbPath.normalize(properties.getPath());

        return switch (mode) {
            case NONE -> permitAll(path);
            case BASIC -> new BasicGothDbAuthorizer(security.getUsername(), resolvePassword(security), security.getRealm());
            case SPRING_SECURITY -> springSecurityAuthorizer(security.getRoles(), springSecurityPresent);
            case AUTO -> throw new IllegalStateException("gothdb.security.mode must be resolved before use");
        };
    }

    @Bean
    @ConditionalOnMissingBean(name = "gothDbSecurityHeadersFilterRegistration")
    FilterRegistrationBean<GothDbSecurityHeadersFilter> gothDbSecurityHeadersFilterRegistration(
            GothDbProperties properties, Environment environment) {
        FilterRegistrationBean<GothDbSecurityHeadersFilter> registration =
                new FilterRegistrationBean<>(new GothDbSecurityHeadersFilter());
        registration.addUrlPatterns(urlPattern(properties, environment));
        registration.setOrder(HEADERS_FILTER_ORDER);
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(name = "gothDbAccessFilterRegistration")
    FilterRegistrationBean<GothDbAccessFilter> gothDbAccessFilterRegistration(
            GothDbAuthorizer authorizer, GothDbProperties properties, Environment environment) {
        FilterRegistrationBean<GothDbAccessFilter> registration =
                new FilterRegistrationBean<>(new GothDbAccessFilter(authorizer));
        registration.addUrlPatterns(urlPattern(properties, environment));
        registration.setOrder(ACCESS_FILTER_ORDER);
        return registration;
    }

    private static GothDbAuthorizer permitAll(String path) {
        logger.warn("GothDB exposes the full contents of the database at " + path
                + " without authentication because gothdb.security.mode is 'none'. "
                + "Restrict access to this path outside the application, or remove the override.");
        return GothDbAuthorizer.permitAll();
    }

    private static GothDbAuthorizer springSecurityAuthorizer(List<String> roles, boolean springSecurityPresent) {
        if (!springSecurityPresent) {
            throw new IllegalStateException("gothdb.security.mode is 'spring-security' but Spring Security "
                    + "is not on the classpath. Add spring-boot-starter-security, or choose another mode.");
        }
        return new SpringSecurityGothDbAuthorizer(roles);
    }

    private static String resolvePassword(GothDbProperties.Security security) {
        String password = security.getPassword();
        if (password != null && !password.isEmpty()) {
            return password;
        }
        String generated = UUID.randomUUID().toString();
        logger.warn(System.lineSeparator() + System.lineSeparator()
                + "Using generated GothDB password: " + generated + System.lineSeparator() + System.lineSeparator()
                + "This generated password is for development use only. "
                + "Set gothdb.security.password, or set gothdb.security.mode explicitly."
                + System.lineSeparator());
        return generated;
    }

    private static String urlPattern(GothDbProperties properties, Environment environment) {
        return servletPath(environment) + GothDbPath.normalize(properties.getPath()) + "/*";
    }

    private static String servletPath(Environment environment) {
        String servletPath = environment.getProperty("spring.mvc.servlet.path", "/");
        if (servletPath == null || servletPath.isBlank() || "/".equals(servletPath)) {
            return "";
        }
        return GothDbPath.normalize(servletPath);
    }

    private static boolean isSpringSecurityPresent(ClassLoader classLoader) {
        return ClassUtils.isPresent(SPRING_SECURITY_MARKER, classLoader);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(SecurityFilterChain.class)
    static class GothDbRequestMatcherConfiguration {

        @Bean
        @ConditionalOnMissingBean
        GothDbRequestMatcher gothDbRequestMatcher(GothDbProperties properties) {
            return new GothDbRequestMatcher(GothDbPath.normalize(properties.getPath()));
        }
    }
}
