package io.github.lessmade.gothdb.core.model;

import java.util.List;
import java.util.Map;

public record RowPage(
        int page,
        int size,
        long totalElements,
        List<Map<String, Object>> rows) {
}
