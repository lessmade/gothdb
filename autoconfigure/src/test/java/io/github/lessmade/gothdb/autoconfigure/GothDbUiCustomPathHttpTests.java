package io.github.lessmade.gothdb.autoconfigure;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = GothDbUiTestApplication.class,
        properties = "gothdb.path=/database")
@AutoConfigureMockMvc
class GothDbUiCustomPathHttpTests {

    private static final Pattern ASSET_PATH = Pattern.compile(
            "(?:src|href)=\"\\./(assets/[^\"]+\\.(?:js|css))\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void movesUiAndApiToCustomPath() throws Exception {
        mockMvc.perform(get("/database"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/database/"));

        String index = mockMvc.perform(get("/database/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = ASSET_PATH.matcher(index);
        assertThat(matcher.find()).isTrue();
        mockMvc.perform(get("/database/" + matcher.group(1)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/database/api/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void noLongerServesDefaultPath() throws Exception {
        mockMvc.perform(get("/gothdb/"))
                .andExpect(status().isNotFound());
    }
}
