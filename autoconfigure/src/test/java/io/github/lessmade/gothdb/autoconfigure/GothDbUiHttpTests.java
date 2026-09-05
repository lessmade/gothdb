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

@SpringBootTest(classes = GothDbUiTestApplication.class)
@AutoConfigureMockMvc
class GothDbUiHttpTests {

    private static final Pattern ASSET_PATH = Pattern.compile(
            "(?:src|href)=\"\\./(assets/[^\"]+\\.(?:js|css))\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void redirectsBasePathToTrailingSlash() throws Exception {
        mockMvc.perform(get("/gothdb"))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", "/gothdb/"));
    }

    @Test
    void servesIndexAndItsBundledAssets() throws Exception {
        String index = mockMvc.perform(get("/gothdb/"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("<div id=\"root\"></div>")))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Matcher matcher = ASSET_PATH.matcher(index);
        int assetCount = 0;
        while (matcher.find()) {
            mockMvc.perform(get("/gothdb/" + matcher.group(1)))
                    .andExpect(status().isOk());
            assetCount++;
        }

        assertThat(assetCount).as("JavaScript and CSS references in index.html").isEqualTo(2);
    }

    @Test
    void keepsApiAvailableAlongsideUi() throws Exception {
        mockMvc.perform(get("/gothdb/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("H2"));
    }
}
