package io.github.lessmade.gothdb.autoconfigure.web;

import java.util.List;

import io.github.lessmade.gothdb.core.model.ColumnInfo;
import io.github.lessmade.gothdb.core.model.SchemaInfo;
import io.github.lessmade.gothdb.core.model.TableInfo;
import io.github.lessmade.gothdb.core.service.DatabaseMetadataService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${gothdb.path:/gothdb}/api")
public class GothDbMetadataController {

    private final DatabaseMetadataService metadataService;

    public GothDbMetadataController(DatabaseMetadataService metadataService) {
        this.metadataService = metadataService;
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
    public List<ColumnInfo> columns(
            @PathVariable("schema") String schema,
            @PathVariable("table") String table) {
        return metadataService.getColumns(schema, table);
    }
}
