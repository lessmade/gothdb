package io.github.lessmade.gothdb.core.dialect;

import java.util.List;

public record PaginatedQuery(String sql, List<Object> parameters) {
}
