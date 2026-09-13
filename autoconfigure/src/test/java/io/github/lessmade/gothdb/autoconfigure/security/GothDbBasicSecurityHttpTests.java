package io.github.lessmade.gothdb.autoconfigure.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GothDbSecurityTestApplication.class, properties = {
        "gothdb.security.mode=basic",
        "gothdb.security.username=gothdb",
        "gothdb.security.password=s3cret",
        "spring.autoconfigure.exclude=org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration" })
@AutoConfigureMockMvc
class GothDbBasicSecurityHttpTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void challengesAnonymousApiRequests() throws Exception {
        mockMvc.perform(get("/gothdb/api/status"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", containsString("Basic realm=\"GothDB\"")))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Authentication required"));
    }

    @Test
    void challengesAnonymousUiRequests() throws Exception {
        mockMvc.perform(get("/gothdb/")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/gothdb")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        mockMvc.perform(get("/gothdb/api/status").with(basic("gothdb", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsMalformedCredentials() throws Exception {
        mockMvc.perform(get("/gothdb/api/status").header("Authorization", "Basic not-base64!"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsConfiguredCredentials() throws Exception {
        mockMvc.perform(get("/gothdb/api/status").with(basic("gothdb", "s3cret")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void addsSecurityHeadersToGothDbResponses() throws Exception {
        mockMvc.perform(get("/gothdb/api/status").with(basic("gothdb", "s3cret")))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Content-Security-Policy", containsString("default-src 'self'")));
    }

    @Test
    void leavesConsumerEndpointsUntouched() throws Exception {
        mockMvc.perform(get("/consumer/ping"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Content-Security-Policy"));
    }

    private static RequestPostProcessor basic(String username, String password) {
        String credentials = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return request -> {
            request.addHeader("Authorization", "Basic " + credentials);
            return request;
        };
    }
}
