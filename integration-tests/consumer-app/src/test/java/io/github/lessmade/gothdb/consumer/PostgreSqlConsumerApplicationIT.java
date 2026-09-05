package io.github.lessmade.gothdb.consumer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(
        classes = GothDbConsumerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostgreSqlConsumerApplicationIT {

    private static final Pattern ASSET_PATH = Pattern.compile(
            "(?:src|href)=\"\\./(assets/[^\"]+\\.(?:js|css))\"");

    @Container
    private static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer("postgres:17.6-alpine")
            .withDatabaseName("gothdb")
            .withUsername("gothdb")
            .withPassword("gothdb");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void configurePostgreSql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
    }

    @Test
    void startsAsExternalConsumerAndServesPostgreSqlData() throws Exception {
        HttpResponse<String> redirect = get("/gothdb");
        assertThat(redirect.statusCode()).isEqualTo(302);
        assertThat(redirect.headers().firstValue("location")).contains("/gothdb/");

        HttpResponse<String> index = get("/gothdb/");
        assertThat(index.statusCode()).isEqualTo(200);
        assertThat(index.headers().firstValue("content-type").orElse("")).startsWith("text/html");
        assertThat(index.body()).contains("<div id=\"root\"></div>");

        Matcher assetMatcher = ASSET_PATH.matcher(index.body());
        assertThat(assetMatcher.find()).isTrue();
        assertThat(get("/gothdb/" + assetMatcher.group(1)).statusCode()).isEqualTo(200);

        HttpResponse<String> status = get("/gothdb/api/status");
        assertThat(status.statusCode()).isEqualTo(200);
        assertThat(status.body()).contains("\"status\":\"UP\"").contains("\"database\":\"PostgreSQL\"");

        HttpResponse<String> schemas = get("/gothdb/api/schemas");
        assertThat(schemas.statusCode()).isEqualTo(200);
        assertThat(schemas.body()).contains("\"name\":\"public\"");

        HttpResponse<String> tables = get("/gothdb/api/schemas/public/tables");
        assertThat(tables.statusCode()).isEqualTo(200);
        assertThat(tables.body()).contains(
                "\"name\":\"friends\"",
                "\"name\":\"products\"",
                "\"name\":\"orders\"",
                "\"name\":\"order_items\"");

        HttpResponse<String> rows = get("/gothdb/api/schemas/public/tables/friends/rows?page=0&size=10");
        assertThat(rows.statusCode()).isEqualTo(200);
        assertThat(rows.body()).contains(
                "\"username\":\"alice\"",
                "\"email\":\"alice@chanes.in\"",
                "\"username\":\"marilyn\"",
                "\"username\":\"emo\"");
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
