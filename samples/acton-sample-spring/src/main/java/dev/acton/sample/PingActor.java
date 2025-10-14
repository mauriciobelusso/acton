package dev.acton.sample;

import dev.acton.core.annotation.Actor;
import dev.acton.core.store.StoreState;
import java.util.Map;

@Actor
public class PingActor {

    private final StoreState<Ping> storePing;

    public PingActor(StoreState<Ping> storePing) {
        this.storePing = storePing;
    }

    public Map<String, String> on(Ping ping) {
        storePing.save(ping);
        return Map.of("Echo", ping.message());
    }
}
