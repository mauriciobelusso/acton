package dev.acton.openapi;

import dev.acton.core.actor.Actor;
import io.swagger.v3.oas.models.OpenAPI;
import java.util.Collection;

public final class OpenApiCache {
    private OpenAPI doc;

    public synchronized OpenAPI build(Collection<Actor> actors) {
        if (doc == null) doc = new OpenApiGenerator().generate(actors);
        return doc;
    }

    public OpenAPI get() { return doc; }
}
