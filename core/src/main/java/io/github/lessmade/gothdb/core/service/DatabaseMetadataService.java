package io.github.lessmade.gothdb.core.service;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import io.github.lessmade.gothdb.core.metadata.DatabaseMetadataException;
import io.github.lessmade.gothdb.core.metadata.SchemaNotFoundException;
import io.github.lessmade.gothdb.core.metadata.TableNotFoundException;
import io.github.lessmade.gothdb.core.model.ColumnInfo;
import io.github.lessmade.gothdb.core.model.DatabaseInfo;
import io.github.lessmade.gothdb.core.model.ForeignKeyInfo;
import io.github.lessmade.gothdb.core.model.IndexInfo;
import io.github.lessmade.gothdb.core.model.PrimaryKeyInfo;
import io.github.lessmade.gothdb.core.model.SchemaInfo;
import io.github.lessmade.gothdb.core.model.TableInfo;

public final class DatabaseMetadataService {

    private static final String[] TABLE_TYPES = { "TABLE", "BASE TABLE", "VIEW" };

    private final DataSource dataSource;

    public DatabaseMetadataService(DataSource dataSource) {
        this.dataSource = dataSource;
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
                schemas.add(new SchemaInfo(
                        resultSet.getString("TABLE_CATALOG"),
                        resultSet.getString("TABLE_SCHEM")));
            }
            return List.copyOf(schemas);
        }
        catch (SQLException exception) {
            throw metadataFailure(exception);
        }
    }

    public List<TableInfo> getTables(String schema) {
        requireName(schema, "schema");

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

    private static DatabaseMetadataException metadataFailure(SQLException exception) {
        return new DatabaseMetadataException("Failed to read database metadata", exception);
    }
}
