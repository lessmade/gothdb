package io.github.lessmade.gothdb.core.value;

public record TextValue(
        String data,
        boolean truncated,
        long length
) {
}
