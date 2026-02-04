package site.tradelink.tradelink.stock.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import site.tradelink.tradelink.stock.sse.dto.SseEvent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientEmitter {

    final String clientId;
    final SseEmitter emitter;
    final BlockingQueue<SseEvent> queue;

    private volatile boolean active = true;

    ClientEmitter(String clientId, SseEmitter emitter, int queueSize) {
        this.clientId = clientId;
        this.emitter = emitter;
        this.queue = new LinkedBlockingQueue<>(queueSize); // backpressure
    }

    boolean isActive() {
        return active;
    }

    void close() {
        active = false;
        // sender Loop 종료 신호
        queue.offer(SseEvent.poison());
    }
}
