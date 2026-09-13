package io.github.lessmade.gothdb.autoconfigure.security;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = GothDbSecurityTestApplication.class, properties = {
        "gothdb.security.mode=none",
        "spring.autoconfigure.exclude=org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration" })
@AutoConfigureMockMvc
class GothDbSecurityDisabledHttpTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesApiWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/gothdb/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void stillAddsSecurityHeaders() throws Exception {
        mockMvc.perform(get("/gothdb/api/status"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }
}
