package io.github.lessmade.gothdb.core.dialect;

public interface DatabaseDialect {

    PaginatedQuery paginate(String selectSql, int page, int size);
}
