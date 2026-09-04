package io.github.lessmade.gothdb.core.schema;

import java.util.List;
import java.util.regex.Pattern;

public final class PatternSchemaFilter implements SchemaFilter {

    public static final List<String> DEFAULT_EXCLUDES = List.of(
            "information_schema",
            "pg_catalog",
            "pg_toast",
            "pg_temp_*",
            "pg_toast_temp_*");

    private final List<Pattern> includes;
    private final List<Pattern> excludes;

    public PatternSchemaFilter(List<String> includes, List<String> excludes) {
        this.includes = compile(includes);
        this.excludes = compile(excludes);
    }

    public static PatternSchemaFilter defaults() {
        return new PatternSchemaFilter(List.of(), DEFAULT_EXCLUDES);
    }

    @Override
    public boolean isVisible(String schema) {
        if (schema == null) {
            return false;
        }
        boolean included = includes.isEmpty() || matches(includes, schema);
        return included && !matches(excludes, schema);
    }

    private static boolean matches(List<Pattern> patterns, String schema) {
        return patterns.stream().anyMatch(pattern -> pattern.matcher(schema).matches());
    }

    private static List<Pattern> compile(List<String> patterns) {
        if (patterns == null) {
            return List.of();
        }
        return patterns.stream()
                .filter(pattern -> pattern != null && !pattern.isBlank())
                .map(PatternSchemaFilter::compileGlob)
                .toList();
    }

    private static Pattern compileGlob(String glob) {
        StringBuilder regex = new StringBuilder("^");
        StringBuilder literal = new StringBuilder();
        for (int index = 0; index < glob.length(); index++) {
            char character = glob.charAt(index);
            if (character == '*' || character == '?') {
                appendQuoted(regex, literal);
                regex.append(character == '*' ? ".*" : ".");
            }
            else {
                literal.append(character);
            }
        }
        appendQuoted(regex, literal);
        return Pattern.compile(regex.append('$').toString());
    }

    private static void appendQuoted(StringBuilder regex, StringBuilder literal) {
        if (!literal.isEmpty()) {
            regex.append(Pattern.quote(literal.toString()));
            literal.setLength(0);
        }
    }
}
