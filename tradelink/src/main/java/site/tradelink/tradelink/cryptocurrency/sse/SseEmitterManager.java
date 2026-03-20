package site.tradelink.tradelink.cryptocurrency.sse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import site.tradelink.tradelink.cryptocurrency.dto.OrderBookDto;
import site.tradelink.tradelink.cryptocurrency.dto.StockPriceSummaryDto;
import site.tradelink.tradelink.cryptocurrency.dto.TradeLogDto;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;
import site.tradelink.tradelink.cryptocurrency.inMemory.StockPriceCache;
import site.tradelink.tradelink.cryptocurrency.sse.dto.SseEvent;

import java.time.LocalDateTime;
import java.util.List;
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
@RequiredArgsConstructor
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

    private final StockPriceCache priceCache;
    private final OrderBookCache orderBookCache;

    /**
     * 종목 상세 SSE 연결
     * 연결 직후 현재가 + 호가 + 최근 체결 내역 캐시 스냅샷을 즉시 push
     */
    public SseEmitter connectTicker(String clientId, String ticker) {
        String key = "T:" + ticker + ":" + clientId;
        disconnect(key);

        SseEmitter emitter = new SseEmitter(timeout);
        ClientEmitter client = new ClientEmitter(key, emitter, queueSize);

        clients.put(key, client);
        connectionCount.incrementAndGet();

        emitter.onCompletion(() -> disconnect(key));
        emitter.onTimeout(() -> disconnect(key));
        emitter.onError(e -> disconnect(key));
        
        startSenderLoop(client);

        // 연결 확인용 이벤트
        enqueue(client, "connect", "connected");

        // 초기 데이터: 현재가
        priceCache.findPrice(ticker)
                .ifPresent(dto -> enqueue(client, "stock-price", dto));

        // 초기 데이터: 호가창
        orderBookCache.findTop5(ticker)
                .ifPresent(dto -> enqueue(client, "order-book", dto));

        // 초기 데이터: 최근 체결 내역
        List<TradeLogDto> logs = priceCache.findTradeLogs(ticker);
        if (!logs.isEmpty()) {
            enqueue(client, "trade-log-init", logs);
        }

        return emitter;
    }

    /** 내 주문 알림 SSE 연결 */
    public SseEmitter connectMember(Long memberSeq, String clientId) {
        return connect("M:" + memberSeq + ":" + clientId);
    }

    private SseEmitter connect(String key) {
        disconnect(key);

        SseEmitter    emitter = new SseEmitter(timeout);
        ClientEmitter client  = new ClientEmitter(key, emitter, queueSize);

        clients.put(key, client);
        connectionCount.incrementAndGet();

        emitter.onCompletion(() -> disconnect(key));
        emitter.onTimeout   (() -> disconnect(key));
        emitter.onError     (e  -> disconnect(key));

        startSenderLoop(client);

        enqueue(client, "connect", "connected");
        return emitter;
    }

    /**
     * broadcast = 큐에만 적재
     */

    // SnapshotScheduler가 호출 - 1초 throttle 적용된 현재가
    public void broadcastPrice(String ticker, StockPriceSummaryDto dto) {
        broadcastToTicker(ticker, "stock-price", dto);
    }

    // SnapshotScheduler가 호출 - 1초 throttle 적용된 호가창
    public void broadcastOrderBook(String ticker, OrderBookDto dto) {
        broadcastToTicker(ticker, "order-book", dto);
    }

    // MatchingEngine이 직접 호출, 해당 멤버만
    public void pushMyOrder(Long memberSeq, String ticker, long price,
                            long quantity, String side, String status,
                            LocalDateTime at) {

        MyOrderDto dto = new MyOrderDto(ticker, price, quantity, side, status, at);
        String prefix = "M:" + memberSeq + ":";

        clients.forEach((key, client) -> {
            if (key.startsWith(prefix)) enqueue(client, "my-order", dto);
        });

    }


    public void broadcastToTicker(String ticker, String eventName, Object data) {
        String prefix = "T:" + ticker + ":";
        clients.forEach((key, client) -> {
            if (key.startsWith(prefix)) {
                if (!enqueue(client, eventName, data)) disconnect(key);
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

                    if (!clients.isEmpty()) {
                        clients.forEach((k, c) -> enqueue(c, "heartbeat", "ping"));
                    }

                } catch (InterruptedException ignored) {
                } catch (Exception e) {
                    log.warn("Heartbeat loop error", e);
                }
            }
        });
    }
}
