package io.github.lessmade.gothdb.autoconfigure.security;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GothDbSecurityTestApplication.class, properties = {
        "gothdb.security.mode=spring-security",
        "gothdb.security.roles=DBA" })
@AutoConfigureMockMvc
@Import(GothDbSpringSecurityHttpTests.PermitAllSecurityConfiguration.class)
class GothDbSpringSecurityHttpTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/gothdb/api/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void rejectsAuthenticatedUserWithoutRequiredRole() throws Exception {
        mockMvc.perform(get("/gothdb/api/status"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied"));
    }

    @Test
    @WithMockUser(roles = "DBA")
    void allowsAuthenticatedUserWithRequiredRole() throws Exception {
        mockMvc.perform(get("/gothdb/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class PermitAllSecurityConfiguration {

        @Bean
        SecurityFilterChain permitAllSecurityFilterChain(HttpSecurity http) throws Exception {
            return http.authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                    .csrf(CsrfConfigurer::disable)
                    .build();
        }
    }
}
