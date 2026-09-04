package io.github.lessmade.gothdb.core.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import io.github.lessmade.gothdb.core.dialect.DatabaseDialect;
import io.github.lessmade.gothdb.core.dialect.LimitOffsetDialect;
import io.github.lessmade.gothdb.core.dialect.PaginatedQuery;
import io.github.lessmade.gothdb.core.exception.DatabaseMetadataException;
import io.github.lessmade.gothdb.core.exception.SchemaNotFoundException;
import io.github.lessmade.gothdb.core.exception.TableNotFoundException;
import io.github.lessmade.gothdb.core.model.ColumnInfo;
import io.github.lessmade.gothdb.core.model.DatabaseInfo;
import io.github.lessmade.gothdb.core.model.ForeignKeyInfo;
import io.github.lessmade.gothdb.core.model.IndexInfo;
import io.github.lessmade.gothdb.core.model.PrimaryKeyInfo;
import io.github.lessmade.gothdb.core.model.RowPage;
import io.github.lessmade.gothdb.core.model.SchemaInfo;
import io.github.lessmade.gothdb.core.model.TableInfo;
import io.github.lessmade.gothdb.core.row.CountMode;
import io.github.lessmade.gothdb.core.row.RowQueryOptions;
import io.github.lessmade.gothdb.core.schema.PatternSchemaFilter;
import io.github.lessmade.gothdb.core.schema.SchemaFilter;
import io.github.lessmade.gothdb.core.value.DefaultJdbcValueConverter;
import io.github.lessmade.gothdb.core.value.JdbcValueConverter;

public final class DatabaseMetadataService {

    private static final String[] TABLE_TYPES = {
            "TABLE", "BASE TABLE", "VIEW", "MATERIALIZED VIEW", "FOREIGN TABLE", "PARTITIONED TABLE"
    };

    private final DataSource dataSource;
    private final DatabaseDialect dialect;
    private final JdbcValueConverter valueConverter;
    private final SchemaFilter schemaFilter;
    private final RowQueryOptions rowQueryOptions;

    public DatabaseMetadataService(DataSource dataSource) {
        this(dataSource, new LimitOffsetDialect(), new DefaultJdbcValueConverter(),
                PatternSchemaFilter.defaults(), RowQueryOptions.DEFAULTS);
    }

    public DatabaseMetadataService(DataSource dataSource, DatabaseDialect dialect) {
        this(dataSource, dialect, new DefaultJdbcValueConverter(), PatternSchemaFilter.defaults(),
                RowQueryOptions.DEFAULTS);
    }

    public DatabaseMetadataService(DataSource dataSource, JdbcValueConverter valueConverter) {
        this(dataSource, new LimitOffsetDialect(), valueConverter, PatternSchemaFilter.defaults(),
                RowQueryOptions.DEFAULTS);
    }

    public DatabaseMetadataService(
            DataSource dataSource, DatabaseDialect dialect, JdbcValueConverter valueConverter) {
        this(dataSource, dialect, valueConverter, PatternSchemaFilter.defaults(), RowQueryOptions.DEFAULTS);
    }

    public DatabaseMetadataService(
            DataSource dataSource, JdbcValueConverter valueConverter, SchemaFilter schemaFilter) {
        this(dataSource, new LimitOffsetDialect(), valueConverter, schemaFilter, RowQueryOptions.DEFAULTS);
    }

    public DatabaseMetadataService(
            DataSource dataSource,
            JdbcValueConverter valueConverter,
            SchemaFilter schemaFilter,
            RowQueryOptions rowQueryOptions) {
        this(dataSource, new LimitOffsetDialect(), valueConverter, schemaFilter, rowQueryOptions);
    }

    public DatabaseMetadataService(
            DataSource dataSource,
            DatabaseDialect dialect,
            JdbcValueConverter valueConverter,
            SchemaFilter schemaFilter,
            RowQueryOptions rowQueryOptions) {
        this.dataSource = dataSource;
        this.dialect = dialect;
        this.valueConverter = valueConverter;
        this.schemaFilter = schemaFilter;
        this.rowQueryOptions = rowQueryOptions;
    }

    public DatabaseInfo getDatabaseInfo() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            return new DatabaseInfo(
                    metadata.getDatabaseProductName(),
                    metadata.getDatabaseProductVersion(),
                    metadata.getDriverName());
        }
        catch (SQLException exception) {
            throw metadataFailure(exception);
        }
    }

    public List<SchemaInfo> getSchemas() {
        try (Connection connection = dataSource.getConnection();
                ResultSet resultSet = connection.getMetaData().getSchemas()) {
            List<SchemaInfo> schemas = new ArrayList<>();
            while (resultSet.next()) {
                String schema = resultSet.getString("TABLE_SCHEM");
                if (schemaFilter.isVisible(schema)) {
                    schemas.add(new SchemaInfo(resultSet.getString("TABLE_CATALOG"), schema));
                }
            }
            return List.copyOf(schemas);
        }
        catch (SQLException exception) {
            throw metadataFailure(exception);
        }
    }

    public List<TableInfo> getTables(String schema) {
        requireName(schema, "schema");
        requireSchemaVisible(schema);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metadata = connection.getMetaData();
            String schemaPattern = escapePattern(metadata, schema);
            requireSchemaExists(metadata, schemaPattern, schema);
            try (ResultSet resultSet = metadata.getTables(null, schemaPattern, "%", TABLE_TYPES)) {
                List<TableInfo> tables = new ArrayList<>();
                while (resultSet.next()) {
                    tables.add(new TableInfo(
                            resultSet.getString("TABLE_CAT"),
                            resultSet.getString("TABLE_SCHEM"),
                            resultSet.getString("TABLE_NAME"),
                            resultSet.getString("TABLE_TYPE"),
                            resultSet.getString("REMARKS")));
                }
                return List.copyOf(tables);
            }
        }
        catch (SQLException exception) {
            throw metadataFailure(exception);
        }
    }

    public List<ColumnInfo> getColumns(String schema, String table) {
        requireName(schema, "schema");
        requireName(table, "table");
        requireSchemaVisible(schema);

        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metadata = connection.getMetaData();
            String schemaPattern = escapePattern(metadata, schema);
            String tablePattern = escapePattern(metadata, table);

            requireSchemaExists(metadata, schemaPattern, schema);
            requireTableExists(metadata, schemaPattern, tablePattern, schema, table);
            try (ResultSet resultSet = metadata.getColumns(null, schemaPattern, tablePattern, "%")) {
                List<ColumnInfo> columns = new ArrayList<>();

                while (resultSet.next()) {

                    columns.add(new ColumnInfo(
                            resultSet.getString("TABLE_CAT"),
                            resultSet.getString("TABLE_SCHEM"),
                            resultSet.getString("TABLE_NAME"),
                            resultSet.getString("COLUMN_NAME"),
                            resultSet.getInt("ORDINAL_POSITION"),
                            resultSet.getInt("DATA_TYPE"),
                            resultSet.getString("TYPE_NAME"),
                            nullableInteger(resultSet, "COLUMN_SIZE"),
                            nullableInteger(resultSet, "DECIMAL_DIGITS"),
                            resultSet.getInt("NULLABLE") == DatabaseMetaData.columnNullable,
                            resultSet.getString("COLUMN_DEF"),
                            "YES".equalsIgnoreCase(resultSet.getString("IS_AUTOINCREMENT"))));
                }
                return List.copyOf(columns);
            }
        }
        catch (SQLException exception) {
            throw metadataFailure(exception);
        }
    }

    public List<PrimaryKeyInfo> getPrimaryKeys(String schema, String table) {
        requireName(schema, "schema");
        requireName(table, "table");
        requireSchemaVisible(schema);

        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metadata = connection.getMetaData();
            requireSchemaAndTableExist(metadata, schema, table);

            try (ResultSet resultSet = metadata.getPrimaryKeys(null, schema, table)) {

                List<PrimaryKeyInfo> primaryKeys = new ArrayList<>();

                while (resultSet.next()) {
                    primaryKeys.add(new PrimaryKeyInfo(
                            resultSet.getString("TABLE_CAT"),
                            resultSet.getString("TABLE_SCHEM"),
                            resultSet.getString("TABLE_NAME"),
                            resultSet.getString("COLUMN_NAME"),
                            resultSet.getInt("KEY_SEQ"),
                            resultSet.getString("PK_NAME")));
                }
                return List.copyOf(primaryKeys);
            }
        }
        catch (SQLException exception) {
            throw metadataFailure(exception);
        }
    }

    public List<ForeignKeyInfo> getForeignKeys(String schema, String table) {

        requireName(schema, "schema");
        requireName(table, "table");
        requireSchemaVisible(schema);

        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metadata = connection.getMetaData();
            requireSchemaAndTableExist(metadata, schema, table);

            try (ResultSet resultSet = metadata.getImportedKeys(null, schema, table)) {

                List<ForeignKeyInfo> foreignKeys = new ArrayList<>();

                while (resultSet.next()) {

                    foreignKeys.add(new ForeignKeyInfo(
                            resultSet.getString("FK_NAME"),
                            resultSet.getString("FKTABLE_CAT"),
                            resultSet.getString("FKTABLE_SCHEM"),
                            resultSet.getString("FKTABLE_NAME"),
                            resultSet.getString("FKCOLUMN_NAME"),
                            resultSet.getString("PKTABLE_CAT"),
                            resultSet.getString("PKTABLE_SCHEM"),
                            resultSet.getString("PKTABLE_NAME"),
                            resultSet.getString("PKCOLUMN_NAME"),
                            resultSet.getInt("KEY_SEQ"),
                            referentialActionName(resultSet.getShort("UPDATE_RULE")),
                            referentialActionName(resultSet.getShort("DELETE_RULE"))));
                }
                return List.copyOf(foreignKeys);
            }
        }
        catch (SQLException exception) {
            throw metadataFailure(exception);
        }
    }

    public List<IndexInfo> getIndexes(String schema, String table) {

        requireName(schema, "schema");
        requireName(table, "table");
        requireSchemaVisible(schema);

        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metadata = connection.getMetaData();
            requireSchemaAndTableExist(metadata, schema, table);

            try (ResultSet resultSet = metadata.getIndexInfo(null, schema, table, false, true)) {

                List<IndexInfo> indexes = new ArrayList<>();

                while (resultSet.next()) {

                    if (resultSet.getShort("TYPE") == DatabaseMetaData.tableIndexStatistic) {
                        continue;
                    }
                    
                    indexes.add(new IndexInfo(
                            resultSet.getString("TABLE_CAT"),
                            resultSet.getString("TABLE_SCHEM"),
                            resultSet.getString("TABLE_NAME"),
                            resultSet.getString("INDEX_NAME"),
                            !resultSet.getBoolean("NON_UNIQUE"),
                            resultSet.getInt("ORDINAL_POSITION"),
                            resultSet.getString("COLUMN_NAME"),
                            resultSet.getString("ASC_OR_DESC")));
                }
                return List.copyOf(indexes);
            }
        }
        catch (SQLException exception) {
            throw metadataFailure(exception);
        }
    }

    public RowPage getRows(String schema, String table, int page, int size) {
        requireName(schema, "schema");
        requireName(table, "table");
        requireSchemaVisible(schema);
        requirePagination(page, size, rowQueryOptions.maxPageSize());

        try (Connection connection = dataSource.getConnection()) {

            DatabaseMetaData metadata = connection.getMetaData();
            requireSchemaAndTableExist(metadata, schema, table);

            String quote = metadata.getIdentifierQuoteString();
            String qualifiedTable = quoteIdentifier(quote, schema) + "." + quoteIdentifier(quote, table);
            RowOrder rowOrder = orderByPrimaryKey(metadata, schema, table, quote);

            Long totalElements = rowQueryOptions.countMode() == CountMode.EXACT
                    ? countRows(connection, qualifiedTable)
                    : null;
            List<Map<String, Object>> rows = selectRows(connection, qualifiedTable, rowOrder.sql(), page, size);

            return new RowPage(page, size, totalElements, rowOrder.stable(), rows);
        }
        catch (SQLException exception) {
            throw metadataFailure(exception);
        }
    }

    private long countRows(Connection connection, String qualifiedTable) throws SQLException {
        String sql = "SELECT COUNT(*) FROM " + qualifiedTable;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            applyQueryTimeout(statement);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        }
    }

    private List<Map<String, Object>> selectRows(
            Connection connection, String qualifiedTable, String orderByClause, int page, int size)
            throws SQLException {
        String baseSql = "SELECT * FROM " + qualifiedTable + orderByClause;
        PaginatedQuery query = dialect.paginate(baseSql, page, size);

        try (PreparedStatement statement = connection.prepareStatement(query.sql())) {
            applyQueryTimeout(statement);

            List<Object> parameters = query.parameters();
            for (int i = 0; i < parameters.size(); i++) {
                statement.setObject(i + 1, parameters.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData columns = resultSet.getMetaData();
                int columnCount = columns.getColumnCount();

                List<Map<String, Object>> rows = new ArrayList<>();
                while (resultSet.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(columns.getColumnLabel(i), valueConverter.convert(resultSet.getObject(i)));
                    }
                    rows.add(row);
                }
                return List.copyOf(rows);
            }
        }
    }

    private static RowOrder orderByPrimaryKey(DatabaseMetaData metadata, String schema, String table, String quote)
            throws SQLException {
        List<PrimaryKeyColumn> keyColumns = new ArrayList<>();
        try (ResultSet resultSet = metadata.getPrimaryKeys(null, schema, table)) {
            while (resultSet.next()) {
                keyColumns.add(new PrimaryKeyColumn(
                        resultSet.getInt("KEY_SEQ"),
                        resultSet.getString("COLUMN_NAME")));
            }
        }
        if (keyColumns.isEmpty()) {
            return new RowOrder("", false);
        }
        keyColumns.sort(Comparator.comparingInt(PrimaryKeyColumn::sequence));
        String sql = " ORDER BY " + keyColumns.stream()
                .map(column -> quoteIdentifier(quote, column.name()))
                .collect(Collectors.joining(", "));
        return new RowOrder(sql, true);
    }

    private static String quoteIdentifier(String quote, String identifier) {
        if (quote == null || quote.isBlank()) {
            return identifier;
        }
        return quote + identifier.replace(quote, quote + quote) + quote;
    }

    private static void requirePagination(int page, int size, int maxPageSize) {
        if (page < 0) {
            throw new IllegalArgumentException("page must not be negative");
        }
        if (size < 1) {
            throw new IllegalArgumentException("size must be at least 1");
        }
        if (size > maxPageSize) {
            throw new IllegalArgumentException("size must not exceed " + maxPageSize);
        }
    }

    private void applyQueryTimeout(PreparedStatement statement) throws SQLException {
        if (!rowQueryOptions.queryTimeout().isZero()) {
            long milliseconds = rowQueryOptions.queryTimeout().toMillis();
            long seconds = Math.max(1, (milliseconds + 999) / 1000);
            statement.setQueryTimeout((int) Math.min(seconds, Integer.MAX_VALUE));
        }
    }

    private static void requireSchemaAndTableExist(DatabaseMetaData metadata, String schema, String table)
            throws SQLException {
        String schemaPattern = escapePattern(metadata, schema);
        String tablePattern = escapePattern(metadata, table);
        requireSchemaExists(metadata, schemaPattern, schema);
        requireTableExists(metadata, schemaPattern, tablePattern, schema, table);
    }

    private static String referentialActionName(short rule) {
        return switch (rule) {
            case DatabaseMetaData.importedKeyCascade -> "CASCADE";
            case DatabaseMetaData.importedKeyRestrict -> "RESTRICT";
            case DatabaseMetaData.importedKeySetNull -> "SET_NULL";
            case DatabaseMetaData.importedKeySetDefault -> "SET_DEFAULT";
            case DatabaseMetaData.importedKeyNoAction -> "NO_ACTION";
            default -> "UNKNOWN";
        };
    }

    private static void requireSchemaExists(DatabaseMetaData metadata, String schemaPattern, String schema)
            throws SQLException {
        try (ResultSet resultSet = metadata.getSchemas(null, schemaPattern)) {
            if (!resultSet.next()) {
                throw new SchemaNotFoundException(schema);
            }
        }
    }

    private static void requireTableExists(
            DatabaseMetaData metadata, String schemaPattern, String tablePattern, String schema, String table)
            throws SQLException {
        try (ResultSet resultSet = metadata.getTables(null, schemaPattern, tablePattern, TABLE_TYPES)) {
            if (!resultSet.next()) {
                throw new TableNotFoundException(schema, table);
            }
        }
    }

    private static String escapePattern(DatabaseMetaData metadata, String value) throws SQLException {
        String escape = metadata.getSearchStringEscape();
        if (escape == null || escape.isEmpty()) {
            return value;
        }
        return value
                .replace(escape, escape + escape)
                .replace("_", escape + "_")
                .replace("%", escape + "%");
    }

    private static Integer nullableInteger(ResultSet resultSet, String column) throws SQLException {
        int value = resultSet.getInt(column);
        return resultSet.wasNull() ? null : value;
    }

    private static void requireName(String value, String parameter) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(parameter + " must not be blank");
        }
    }

    private void requireSchemaVisible(String schema) {
        if (!schemaFilter.isVisible(schema)) {
            throw new SchemaNotFoundException(schema);
        }
    }

    private static DatabaseMetadataException metadataFailure(SQLException exception) {
        return new DatabaseMetadataException("Failed to read database metadata", exception);
    }

    private record PrimaryKeyColumn(int sequence, String name) {
    }

    private record RowOrder(String sql, boolean stable) {
    }
}
