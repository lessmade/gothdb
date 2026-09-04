package io.github.lessmade.gothdb.core.value;

import java.lang.reflect.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLData;
import java.sql.SQLXML;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class DefaultJdbcValueConverter implements JdbcValueConverter {

    public static final int DEFAULT_MAX_VALUE_LENGTH = 1_048_576;

    private final int maxValueLength;

    public DefaultJdbcValueConverter() {
        this(DEFAULT_MAX_VALUE_LENGTH);
    }

    public DefaultJdbcValueConverter(int maxValueLength) {
        if (maxValueLength < 1) {
            throw new IllegalArgumentException("maxValueLength must be at least 1");
        }
        this.maxValueLength = maxValueLength;
    }

    @Override
    public Object convert(Object value) throws SQLException {
        if (value == null || isJsonScalar(value) || value instanceof TemporalAccessor
                || value instanceof java.util.Date || value instanceof UUID) {
            return value;
        }
        if (value instanceof byte[] bytes) {
            return binaryValue(bytes, bytes.length);
        }
        if (value instanceof java.sql.Array sqlArray) {
            return arrayValue(sqlArray);
        }
        if (value instanceof Blob blob) {
            return blobValue(blob);
        }
        if (value instanceof Clob clob) {
            return clobValue(clob);
        }
        if (value instanceof SQLXML sqlxml) {
            return sqlXmlValue(sqlxml);
        }
        if (value instanceof SQLData sqlData) {
            return new UnsupportedJdbcValue(sqlData.getSQLTypeName(), String.valueOf(value));
        }
        return new UnsupportedJdbcValue(value.getClass().getName(), String.valueOf(value));
    }

    private List<Object> arrayValue(java.sql.Array sqlArray) throws SQLException {
        try {
            Object array = sqlArray.getArray();
            int length = Array.getLength(array);
            List<Object> values = new ArrayList<>(length);
            for (int index = 0; index < length; index++) {
                values.add(convert(Array.get(array, index)));
            }
            return Collections.unmodifiableList(values);
        }
        finally {
            sqlArray.free();
        }
    }

    private BinaryValue blobValue(Blob blob) throws SQLException {
        try {
            long length = blob.length();
            byte[] bytes = blob.getBytes(1, boundedLength(length));
            return binaryValue(bytes, length);
        }
        finally {
            blob.free();
        }
    }

    private TextValue clobValue(Clob clob) throws SQLException {
        try {
            long length = clob.length();
            return new TextValue(clob.getSubString(1, boundedLength(length)), length > maxValueLength, length);
        }
        finally {
            clob.free();
        }
    }

    private TextValue sqlXmlValue(SQLXML sqlxml) throws SQLException {
        try {
            String value = sqlxml.getString();
            int returnedLength = Math.min(value.length(), maxValueLength);
            return new TextValue(value.substring(0, returnedLength), value.length() > maxValueLength, value.length());
        }
        finally {
            sqlxml.free();
        }
    }

    private BinaryValue binaryValue(byte[] bytes, long length) {
        int returnedLength = Math.min(bytes.length, maxValueLength);
        byte[] returnedBytes = returnedLength == bytes.length
                ? bytes
                : java.util.Arrays.copyOf(bytes, returnedLength);
        return new BinaryValue(
                "base64",
                Base64.getEncoder().encodeToString(returnedBytes),
                length > maxValueLength,
                length);
    }

    private int boundedLength(long length) {
        return (int) Math.min(length, maxValueLength);
    }

    private static boolean isJsonScalar(Object value) {
        return value instanceof String || value instanceof Number || value instanceof Boolean
                || value instanceof Character;
    }
}
