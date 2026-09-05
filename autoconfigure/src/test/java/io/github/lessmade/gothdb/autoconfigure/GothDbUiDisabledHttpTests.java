package io.github.lessmade.gothdb.autoconfigure;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = GothDbUiTestApplication.class,
        properties = "gothdb.ui.enabled=false")
@AutoConfigureMockMvc
class GothDbUiDisabledHttpTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void disablesOnlyUi() throws Exception {
        mockMvc.perform(get("/gothdb"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/gothdb/"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/gothdb/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
