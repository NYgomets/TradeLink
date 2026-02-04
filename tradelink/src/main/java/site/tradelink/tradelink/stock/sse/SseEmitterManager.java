package site.tradelink.tradelink.stock.sse;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import site.tradelink.tradelink.stock.sse.dto.SseEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * SSE Emitter 관리
 */
@Slf4j
@Component
public class SseEmitterManager {

    @Value("${sse.timeout:1800000}")
    private long timeout;

    @Value("${sse.queue-size:100}")
    private int queueSize;

    @Value("${sse.heartbeat-interval:30000}")
    private long heartbeatInterval;

    private final Map<String, ClientEmitter> clients = new ConcurrentHashMap<>();
    private final AtomicInteger connectionCount = new AtomicInteger(0);

    private final ExecutorService senderPool = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 연결
     */
    public SseEmitter connect(String clientId) {
        disconnect(clientId);

        SseEmitter emitter = new SseEmitter(timeout);
        ClientEmitter client = new ClientEmitter(clientId, emitter, queueSize);

        clients.put(clientId, client);
        connectionCount.incrementAndGet();

        emitter.onCompletion(() -> disconnect(clientId));
        emitter.onTimeout(() -> disconnect(clientId));
        emitter.onError(e -> disconnect(clientId));
        
        startSenderLoop(client);

        // 연결 확인용 이벤트
        enqueue(client, "connect", "connected");

        return emitter;
    }

    /**
     * broadcast = 큐에만 적재
     */
    public void broadcast(String eventName, Object data) {
        clients.values().forEach(client -> {
            if (!enqueue(client, eventName, data)) {
                disconnect(client.clientId);
            }
        });
    }

    /**
     * 클라이언트별 전송 루프
     */
    private void startSenderLoop(ClientEmitter client) {
        senderPool.submit(() -> {
            try {
                while (client.isActive()) {
                    SseEvent event = client.queue.take();

                    if (event.isPoison()) {
                        break;
                    }

                    client.emitter.send(
                            SseEmitter.event()
                                    .name(event.eventName())
                                    .data(event.data())
                    );
                }
            } catch (Exception e) {
                log.debug("Sender loop terminated: {}", client.clientId);
            } finally {
                disconnect(client.clientId);
            }
        });
    }

    /**
     * 최초 연결 확인 용 더미 데이터
     */
    private boolean enqueue(ClientEmitter client, String eventName, Object data) {
        return client.queue.offer(new SseEvent(eventName, data));
    }

    private void disconnect(String clientId) {
        ClientEmitter client = clients.remove(clientId);

        if (client == null) {
            return;
        }

        client.close();
        connectionCount.decrementAndGet();

        try {
            client.emitter.complete();
        } catch (Exception ignored) {}
    }

    /**
     * Heartbeat Loop
     */
    @PostConstruct
    public void startHeartbeat() {
        Thread.startVirtualThread(() -> {
            while (true) {
                try {
                    Thread.sleep(heartbeatInterval);

                    if (clients.isEmpty()) {
                        continue;
                    }

                    broadcast("heartbeat", null);

                } catch (InterruptedException ignored) {
                } catch (Exception e) {
                    log.warn("Heartbeat loop error", e);
                }
            }
        });
    }
}
