package dev.acton.router;

import dev.acton.core.actor.Actor;
import dev.acton.core.annotation.Contract;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RouterTest {

    @Contract("ping.echo")
    record Ping(String message) {}

    static class PingActor implements Actor {
        public String on(Ping p) { return "Echo: " + p.message(); }
    }

    @Test
    void shouldExecuteContractViaRouter() {
        var router = new Router();
        router.registerActor(new PingActor());

        var result = router.execute(new Ping("hello"));
        assertEquals("Echo: hello", result);
        assertEquals(1, router.routes().size());
    }
}
