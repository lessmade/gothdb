package io.github.lessmade.gothdb.core.dialect;

import java.util.List;

public final class LimitOffsetDialect implements DatabaseDialect {

    @Override
    public PaginatedQuery paginate(String selectSql, int page, int size) {
        long offset = (long) page * size;
        return new PaginatedQuery(selectSql + " LIMIT ? OFFSET ?", List.of(size, offset));
    }
}
