package dev.acton.sample;

import dev.acton.core.annotation.Bind;
import dev.acton.core.store.Store;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PingService {

    private final Store<Ping> storePing;

    public PingService(Store<Ping> storePing) {
        this.storePing = storePing;
    }

    @Bind(Ping.class)
    public Map<String, String> on(Ping ping) {
        storePing.save(ping);
        return Map.of("Echo", ping.message());
    }
}
