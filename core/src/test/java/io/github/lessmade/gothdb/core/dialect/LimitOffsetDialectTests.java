package io.github.lessmade.gothdb.core.dialect;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LimitOffsetDialectTests {

    private final DatabaseDialect dialect = new LimitOffsetDialect();

    @Test
    void appendsLimitAndOffsetInBindOrder() {
        PaginatedQuery query = dialect.paginate("SELECT * FROM \"APP\".\"CUSTOMER\"", 0, 20);

        assertThat(query.sql()).isEqualTo("SELECT * FROM \"APP\".\"CUSTOMER\" LIMIT ? OFFSET ?");
        assertThat(query.parameters()).containsExactly(20, 0L);
    }

    @Test
    void computesOffsetFromPageAndSize() {
        PaginatedQuery query = dialect.paginate("SELECT * FROM T", 3, 25);

        assertThat(query.parameters()).containsExactly(25, 75L);
    }
}
