package io.github.lessmade.gothdb.autoconfigure.security;

import java.util.Collection;

import javax.sql.DataSource;

import io.github.lessmade.gothdb.autoconfigure.config.GothDbAutoConfiguration;
import io.github.lessmade.gothdb.autoconfigure.config.GothDbSecurityAutoConfiguration;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

class GothDbSecurityAutoConfigurationTests {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GothDbAutoConfiguration.class, GothDbSecurityAutoConfiguration.class))
            .withBean(DataSource.class, GothDbSecurityAutoConfigurationTests::dataSource);

    @Test
    void backsOffWithoutDataSource() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(GothDbSecurityAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(GothDbAuthorizer.class));
    }

    @Test
    void delegatesToSpringSecurityWhenItIsOnTheClasspath() {
        contextRunner.run(context -> assertThat(context.getBean(GothDbAuthorizer.class))
                .isInstanceOf(SpringSecurityGothDbAuthorizer.class));
    }

    @Test
    void fallsBackToBasicAuthenticationWithoutSpringSecurity() {
        contextRunner.withClassLoader(new FilteredClassLoader("org.springframework.security.core.context.SecurityContextHolder"))
                .run(context -> assertThat(context.getBean(GothDbAuthorizer.class))
                        .isInstanceOf(BasicGothDbAuthorizer.class));
    }

    @Test
    void generatesAPasswordWhenBasicAuthenticationHasNoneConfigured() {
        contextRunner.withPropertyValues("gothdb.security.mode=basic")
                .run(context -> assertThat(context.getBean(GothDbAuthorizer.class))
                        .isInstanceOf(BasicGothDbAuthorizer.class));
    }

    @Test
    void failsFastWhenSpringSecurityModeIsChosenWithoutSpringSecurity() {
        contextRunner.withClassLoader(new FilteredClassLoader("org.springframework.security.core.context.SecurityContextHolder"))
                .withPropertyValues("gothdb.security.mode=spring-security")
                .run(context -> assertThat(context).hasFailed());
    }

    @Test
    void permitsEverythingWhenExplicitlyDisabled() {
        contextRunner.withPropertyValues("gothdb.security.mode=none")
                .run(context -> assertThat(context.getBean(GothDbAuthorizer.class))
                        .isNotInstanceOfAny(BasicGothDbAuthorizer.class, SpringSecurityGothDbAuthorizer.class));
    }

    @Test
    void registersFiltersForTheConfiguredPath() {
        contextRunner.withPropertyValues("gothdb.path=/database").run(context -> {
            assertThat(urlPatterns(context, "gothDbAccessFilterRegistration")).containsExactly("/database/*");
            assertThat(urlPatterns(context, "gothDbSecurityHeadersFilterRegistration")).containsExactly("/database/*");
        });
    }

    @Test
    void accountsForACustomDispatcherServletPath() {
        contextRunner.withPropertyValues("spring.mvc.servlet.path=/mvc")
                .run(context -> assertThat(urlPatterns(context, "gothDbAccessFilterRegistration"))
                        .containsExactly("/mvc/gothdb/*"));
    }

    @Test
    void runsTheAccessFilterAfterSpringSecurityAndAfterTheHeadersFilter() {
        contextRunner.run(context -> {
            int headers = context.getBean("gothDbSecurityHeadersFilterRegistration", FilterRegistrationBean.class).getOrder();
            int access = context.getBean("gothDbAccessFilterRegistration", FilterRegistrationBean.class).getOrder();

            assertThat(headers).isGreaterThan(-100);
            assertThat(access).isGreaterThan(headers);
        });
    }

    @Test
    void exposesARequestMatcherForConsumerSecurityRules() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(GothDbRequestMatcher.class));
    }

    private static Collection<String> urlPatterns(AssertableWebApplicationContext context, String beanName) {
        return context.getBean(beanName, FilterRegistrationBean.class).getUrlPatterns();
    }

    private static DataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:gothdb-security-config;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        return dataSource;
    }
}
