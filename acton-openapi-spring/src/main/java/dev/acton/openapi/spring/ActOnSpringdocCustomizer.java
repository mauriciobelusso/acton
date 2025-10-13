package dev.acton.openapi.spring;

import dev.acton.openapi.OpenApiGenerator;
import dev.acton.openapi.OpenApiMerge;
import dev.acton.router.Router;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.customizers.OpenApiCustomizer;

/**
 * Injects ActOn-generated Paths and Components into Springdoc's OpenAPI model.
 */
final class ActOnSpringdocCustomizer implements OpenApiCustomizer {

    private final Router router;
    private final OpenApiGenerator generator;

    ActOnSpringdocCustomizer(Router router, OpenApiGenerator generator) {
        this.router = router;
        this.generator = generator;
    }

    @Override
    public void customise(OpenAPI springdocModel) {
        OpenAPI actonApi = generator.generate(router.routes(), "ActOn", "0.1.0", "/");

        OpenApiMerge.into(springdocModel, actonApi);
    }
}
