package dev.acton.openapi;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import java.util.LinkedHashMap;
import java.util.Map;

public final class OpenApiMerge {
    private OpenApiMerge() {}

    public static void into(OpenAPI target, OpenAPI add) {
        if (target == null || add == null) return;

        // ---- Components ----
        var addComp = add.getComponents();
        if (addComp != null) {
            var tgtComp = nonNullComponents(target);

            // Schemas
            mergeMap(addComp.getSchemas(), tgtComp.getSchemas(), tgtComp::setSchemas);

            // Parameters
            mergeMap(addComp.getParameters(), tgtComp.getParameters(), tgtComp::setParameters);

            // Responses
            mergeMap(addComp.getResponses(), tgtComp.getResponses(), tgtComp::setResponses);

            // (adicione outros conforme precisar: headers, requestBodies, securitySchemes, etc.)
        }

        // ---- Paths ----
        if (add.getPaths() != null) {
            if (target.getPaths() == null) target.setPaths(new io.swagger.v3.oas.models.Paths());
            add.getPaths().forEach((path, incoming) -> {
                PathItem existing = target.getPaths().get(path);
                target.getPaths().addPathItem(path, mergePath(existing, incoming));
            });
        }
    }

    private static Components nonNullComponents(OpenAPI api) {
        var comp = api.getComponents();
        if (comp == null) {
            comp = new Components();
            api.setComponents(comp);
        }
        // garanta mapas inicializados (evita NPE em getters):
        if (comp.getSchemas() == null) comp.setSchemas(new LinkedHashMap<>());
        if (comp.getParameters() == null) comp.setParameters(new LinkedHashMap<>());
        if (comp.getResponses() == null) comp.setResponses(new LinkedHashMap<>());
        return comp;
    }

    private static <K, V> void mergeMap(Map<K, V> src, Map<K, V> dst, java.util.function.Consumer<Map<K, V>> dstSetter) {
        if (src == null || src.isEmpty()) return;
        if (dst == null) {
            dst = new LinkedHashMap<>();
            dstSetter.accept(dst);
        }
        dst.putAll(src);
    }

    private static PathItem mergePath(PathItem a, PathItem b) {
        if (a == null) return b;
        if (b.getGet() != null) a.setGet(b.getGet());
        if (b.getPost() != null) a.setPost(b.getPost());
        if (b.getPut() != null) a.setPut(b.getPut());
        if (b.getDelete() != null) a.setDelete(b.getDelete());
        if (b.getPatch() != null) a.setPatch(b.getPatch());
        if (b.getHead() != null) a.setHead(b.getHead());
        if (b.getOptions() != null) a.setOptions(b.getOptions());
        if (b.getTrace() != null) a.setTrace(b.getTrace());
        return a;
    }
}
