package io.github.lessmade.gothdb.autoconfigure.web;

import io.github.lessmade.gothdb.core.model.DatabaseInfo;
import io.github.lessmade.gothdb.core.service.DatabaseMetadataService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${gothdb.path:/gothdb}/api")
public class GothDbStatusController {

    private final DatabaseMetadataService metadataService;

    public GothDbStatusController(DatabaseMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping("/status")
    public GothDbStatus status() {
        DatabaseInfo databaseInfo = metadataService.getDatabaseInfo();
        return new GothDbStatus(
                "UP",
                databaseInfo.product(),
                databaseInfo.version(),
                databaseInfo.driver());
    }
}
