package io.github.lessmade.gothdb.autoconfigure.web;

import java.util.List;

import io.github.lessmade.gothdb.core.model.ColumnInfo;
import io.github.lessmade.gothdb.core.model.ForeignKeyInfo;
import io.github.lessmade.gothdb.core.model.IndexInfo;
import io.github.lessmade.gothdb.core.model.PrimaryKeyInfo;
import io.github.lessmade.gothdb.core.model.RowPage;
import io.github.lessmade.gothdb.core.model.SchemaInfo;
import io.github.lessmade.gothdb.core.model.TableInfo;
import io.github.lessmade.gothdb.core.service.DatabaseMetadataService;
import io.github.lessmade.gothdb.core.row.RowQueryOptions;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${gothdb.path:/gothdb}/api")
public class GothDbMetadataController {

    private final DatabaseMetadataService metadataService;
    private final int defaultPageSize;

    public GothDbMetadataController(DatabaseMetadataService metadataService) {
        this(metadataService, RowQueryOptions.DEFAULTS);
    }

    public GothDbMetadataController(DatabaseMetadataService metadataService, RowQueryOptions rowQueryOptions) {
        this.metadataService = metadataService;
        this.defaultPageSize = Math.min(50, rowQueryOptions.maxPageSize());
    }

    @GetMapping("/schemas")
    public List<SchemaInfo> schemas() {

        return metadataService.getSchemas();
    }

    @GetMapping("/schemas/{schema}/tables")
    public List<TableInfo> tables(@PathVariable("schema") String schema) {

        return metadataService.getTables(schema);
    }

    @GetMapping("/schemas/{schema}/tables/{table}/columns")
    public List<ColumnInfo> columns(@PathVariable("schema") String schema, @PathVariable("table") String table) {

        return metadataService.getColumns(schema, table);
    }

    @GetMapping("/schemas/{schema}/tables/{table}/primary-key")
    public List<PrimaryKeyInfo> primaryKey(@PathVariable("schema") String schema, @PathVariable("table") String table) {

        return metadataService.getPrimaryKeys(schema, table);
    }

    @GetMapping("/schemas/{schema}/tables/{table}/foreign-keys")
    public List<ForeignKeyInfo> foreignKeys(@PathVariable("schema") String schema, @PathVariable("table") String table) {

        return metadataService.getForeignKeys(schema, table);
    }

    @GetMapping("/schemas/{schema}/tables/{table}/indexes")
    public List<IndexInfo> indexes(@PathVariable("schema") String schema, @PathVariable("table") String table) {

        return metadataService.getIndexes(schema, table);
    }

    @GetMapping("/schemas/{schema}/tables/{table}/rows")
    public RowPage rows(@PathVariable("schema") String schema, @PathVariable("table") String table,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", required = false) Integer size) {
        
        return metadataService.getRows(schema, table, page, size == null ? defaultPageSize : size);
    }
}
