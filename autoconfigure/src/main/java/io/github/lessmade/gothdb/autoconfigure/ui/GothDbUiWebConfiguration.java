package io.github.lessmade.gothdb.autoconfigure.ui;

import io.github.lessmade.gothdb.autoconfigure.config.GothDbProperties;

import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public final class GothDbUiWebConfiguration implements WebMvcConfigurer {

    private static final String UI_RESOURCES = "classpath:/META-INF/gothdb/";

    private final String path;

    public GothDbUiWebConfiguration(GothDbProperties properties) {
        this.path = GothDbUiController.normalizePath(properties.getPath());
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(path + "/**")
                .addResourceLocations(UI_RESOURCES);
    }
}
