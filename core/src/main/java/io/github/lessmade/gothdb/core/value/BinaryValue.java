package io.github.lessmade.gothdb.core.value;

public record BinaryValue(
        String encoding,
        String data,
        boolean truncated,
        long length
) {
}
