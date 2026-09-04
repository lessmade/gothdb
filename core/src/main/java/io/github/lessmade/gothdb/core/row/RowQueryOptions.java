package io.github.lessmade.gothdb.core.row;

import java.time.Duration;

public record RowQueryOptions(
        CountMode countMode,
        int maxPageSize,
        Duration queryTimeout) {

    public static final RowQueryOptions DEFAULTS = new RowQueryOptions(CountMode.EXACT, 200, Duration.ofSeconds(5));

    public RowQueryOptions {
        if (countMode == null) {
            throw new IllegalArgumentException("countMode must not be null");
        }
        if (maxPageSize < 1) {
            throw new IllegalArgumentException("maxPageSize must be at least 1");
        }
        if (queryTimeout == null || queryTimeout.isNegative()) {
            throw new IllegalArgumentException("queryTimeout must not be null or negative");
        }
    }
}
