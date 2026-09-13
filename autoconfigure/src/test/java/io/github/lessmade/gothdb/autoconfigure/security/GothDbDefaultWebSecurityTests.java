package io.github.lessmade.gothdb.autoconfigure.security;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GothDbSecurityTestApplication.class)
@AutoConfigureMockMvc
class GothDbDefaultWebSecurityTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void keepsBootDefaultSecurityFilterChain() {
        assertThat(context.getBeansOfType(SecurityFilterChain.class)).hasSize(1);
        assertThat(context.getBeanNamesForType(SecurityFilterChain.class))
                .containsExactly("defaultSecurityFilterChain");
    }

    @Test
    void leavesConsumerEndpointsProtectedByBootDefaults() throws Exception {
        mockMvc.perform(get("/consumer/ping")).andExpect(status().isUnauthorized());
    }

    @Test
    void protectsGothDbEndpointsToo() throws Exception {
        mockMvc.perform(get("/gothdb/api/status")).andExpect(status().isUnauthorized());
    }

    @Test
    void exposesRequestMatcherForConsumerSecurityRules() {
        assertThat(context.getBean(GothDbRequestMatcher.class)).isNotNull();
    }
}
