package dev.acton.core.bind;

public interface ActOnBinder {
    interface Handler<Q> { Object handle(Q contract) throws Exception; }
    <Q> void handle(Class<Q> contractType, Handler<Q> handler);
}