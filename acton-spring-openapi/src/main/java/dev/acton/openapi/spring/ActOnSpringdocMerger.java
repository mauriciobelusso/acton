package dev.acton.openapi.spring;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import java.util.LinkedHashMap;

final class ActOnSpringdocMerger {
    private ActOnSpringdocMerger() {}

    static void mergeInto(OpenAPI target, OpenAPI add) {
        if (target == null || add == null) return;

        // Components
        if (add.getComponents() != null) {
            if (target.getComponents() == null) target.setComponents(new io.swagger.v3.oas.models.Components());
            var t = target.getComponents();
            var a = add.getComponents();

            if (a.getSchemas() != null) {
                if (t.getSchemas() == null) t.setSchemas(new LinkedHashMap<>());
                t.getSchemas().putAll(a.getSchemas());
            }
            if (a.getParameters() != null) {
                if (t.getParameters() == null) t.setParameters(new LinkedHashMap<>());
                t.getParameters().putAll(a.getParameters());
            }
            if (a.getResponses() != null) {
                if (t.getResponses() == null) t.setResponses(new LinkedHashMap<>());
                t.getResponses().putAll(a.getResponses());
            }
        }

        // Paths
        if (add.getPaths() != null) {
            if (target.getPaths() == null) target.setPaths(new io.swagger.v3.oas.models.Paths());
            add.getPaths().forEach((p, incoming) -> {
                PathItem existing = target.getPaths().get(p);
                target.getPaths().addPathItem(p, mergePath(existing, incoming));
            });
        }
    }

    private static PathItem mergePath(PathItem a, PathItem b) {
        if (a == null) return b;
        if (b.getGet()     != null) a.setGet(b.getGet());
        if (b.getPost()    != null) a.setPost(b.getPost());
        if (b.getPut()     != null) a.setPut(b.getPut());
        if (b.getDelete()  != null) a.setDelete(b.getDelete());
        if (b.getPatch()   != null) a.setPatch(b.getPatch());
        if (b.getOptions() != null) a.setOptions(b.getOptions());
        if (b.getHead()    != null) a.setHead(b.getHead());
        if (b.getTrace()   != null) a.setTrace(b.getTrace());
        return a;
    }
}
