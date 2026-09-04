package io.github.lessmade.gothdb.core.value;

import java.sql.SQLException;

@FunctionalInterface
public interface JdbcValueConverter {

    Object convert(Object value) throws SQLException;
}
