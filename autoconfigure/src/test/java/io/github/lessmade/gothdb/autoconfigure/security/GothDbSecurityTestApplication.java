package io.github.lessmade.gothdb.autoconfigure.security;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootConfiguration
@EnableAutoConfiguration
class GothDbSecurityTestApplication {

    @Bean
    DataSource dataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:gothdb-security;DB_CLOSE_DELAY=-1");
        dataSource.setUser("sa");
        return dataSource;
    }

    @RestController
    static class ConsumerController {

        @GetMapping("/consumer/ping")
        String ping() {
            return "pong";
        }
    }
}
