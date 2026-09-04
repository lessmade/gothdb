package io.github.lessmade.gothdb.core.value;

import java.sql.Connection;
import java.sql.SQLXML;
import java.time.OffsetDateTime;
import java.util.Arrays;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultJdbcValueConverterTests {

    @Test
    void preservesStandardValues() throws Exception {
        DefaultJdbcValueConverter converter = new DefaultJdbcValueConverter();
        OffsetDateTime timestamp = OffsetDateTime.parse("2024-01-02T03:04:05Z");

        assertThat(converter.convert(null)).isNull();
        assertThat(converter.convert("text")).isEqualTo("text");
        assertThat(converter.convert(42)).isEqualTo(42);
        assertThat(converter.convert(timestamp)).isSameAs(timestamp);
    }

    @Test
    void convertsAndReleasesJdbcValues() throws Exception {
        DefaultJdbcValueConverter converter = new DefaultJdbcValueConverter(4);
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:value-converter");

        try (Connection connection = dataSource.getConnection()) {
            java.sql.Array array = connection.createArrayOf("VARCHAR", new Object[] { "one", null, "three" });
            assertThat(converter.convert(array)).isEqualTo(Arrays.asList("one", null, "three"));

            BinaryValue blob = (BinaryValue) converter.convert(connection.createBlob());
            assertThat(blob).isEqualTo(new BinaryValue("base64", "", false, 0));

            java.sql.Clob clobValue = connection.createClob();
            clobValue.setString(1, "abcdef");
            TextValue clob = (TextValue) converter.convert(clobValue);
            assertThat(clob).isEqualTo(new TextValue("abcd", true, 6));

            SQLXML xmlValue = connection.createSQLXML();
            xmlValue.setString("<root/>");
            TextValue xml = (TextValue) converter.convert(xmlValue);
            assertThat(xml).isEqualTo(new TextValue("<roo", true, 7));
        }
    }

    @Test
    void convertsBinaryAndUnknownValuesToStableRepresentations() throws Exception {
        DefaultJdbcValueConverter converter = new DefaultJdbcValueConverter(2);

        assertThat(converter.convert(new byte[] { 1, 2, 3 }))
                .isEqualTo(new BinaryValue("base64", "AQI=", true, 3));
        assertThat(converter.convert(new VendorValue("payload")))
                .isEqualTo(new UnsupportedJdbcValue(VendorValue.class.getName(), "payload"));
    }

    private record VendorValue(String value) {
        @Override
        public String toString() {
            return value;
        }
    }
}
