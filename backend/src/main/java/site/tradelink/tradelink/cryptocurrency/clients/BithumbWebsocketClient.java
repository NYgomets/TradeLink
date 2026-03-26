package site.tradelink.tradelink.cryptocurrency.clients;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import site.tradelink.tradelink.cryptocurrency.handler.BithumbOrderBookHandler;
import site.tradelink.tradelink.cryptocurrency.handler.BithumbOrderBookSnapshotHandler;
import site.tradelink.tradelink.cryptocurrency.handler.BithumbTickerHandler;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 빗썸 WebSocket 클라이언트
 *
 * 구독 타입:
 * - ticker: 현재가 -> BithumbTickerHandler
 * - orderbooksnapshot: 초기 전체 호가 -> BithumbOrderBookSnapshotHandler
 * - orderBookDepth: 호가 변경분 -> BithumbOrderBookHandler
 *
 * 호가 수신 순서:
 * 1. 연결 직후 orderbooksnapshot 구독 -> 전체 30호가 수신 -> OrderBookCache 초기화
 * 2. 이후 orderbookdepth로 변경된 레벨만 수신 -> 부분 업데이트
 *
 * 재연결 전략:
 * - 5회까지 : 지수 백오프 (2s → 4s → 8s → 16s → 30s 상한)
 * - 5회 초과 : 5분 대기 후 카운터 리셋 → 무한 재시도
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BithumbWebsocketClient implements WebSocketHandler {

    private static final String WS_URL = "wss://pubwss.bithumb.com/pub/ws";
    private static final int MAX_RECONNECT = 5;
    private static final long RECONNECT_BASE_MS = 1_000;
    private static final long   RESET_AFTER_MS    = 5 * 60 * 1_000L;

    // 구독할 심볼 목록 (빗썸 형식: BASE_KRW)
    private static final List<String> SYMBOLS = List.of(
            "BTC_KRW", "ETH_KRW", "XRP_KRW", "ADA_KRW",
            "DOGE_KRW", "LINK_KRW", "TRX_KRW",
            "LTC_KRW", "BCH_KRW", "ETC_KRW"
    );

    private final BithumbTickerHandler tickerHandler;
    private final BithumbOrderBookSnapshotHandler snapshotHandler;
    private final BithumbOrderBookHandler orderBookHandler;
    private final ObjectMapper objectMapper;

    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);
    // 중복 재연결 방지용 플래그
    private final AtomicBoolean isConnecting =  new AtomicBoolean(false);

    @EventListener(ApplicationReadyEvent.class)
    public void connect() {
        connectInternal();
    }

    // 연결
    private void connectInternal() {
        if (!isConnecting.compareAndSet(false, true)) {
            return;
        }

        StandardWebSocketClient client = new StandardWebSocketClient();
        client.execute(this, WS_URL) // CompletableFuture<WebSocketSession>
                .whenComplete((session, ex) -> {
                    isConnecting.set(false);

                    if (ex != null) {
                        log.error("[Bithumb WS] 연결 실패: {}", ex.getMessage());
                        scheduleReconnect();
                    }
                    // 성공 시 afterConnectionEstablished() 에서 처리
                });
    }

    // WebSocketHandler 구현

    @Override
    public void afterConnectionEstablished(org.springframework.web.socket.WebSocketSession session) throws Exception {
        reconnectAttempts.set(0);
        log.info("[Bithumb WS] 연결 성공");

        // 1. 현재가 구독
        subscribe(session, "ticker");

        // 2. 초기 전체 호가 스냅샷 구독 (연결 직후 1회)
        subscribe(session, "orderbooksnapshot");

        // 3. 이후 변경분 구독
        subscribe(session, "orderbookdepth");
    }

    @Override
    public void handleMessage(org.springframework.web.socket.WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = message.getPayload().toString();
            JsonNode root = objectMapper.readTree(payload);
            String type = root.path("type").asText();

            switch (type) {
                case "ticker" -> tickerHandler.handle(root);
                case "orderbooksnapshot" -> snapshotHandler.handle(root);
                case  "orderbookdepth" -> orderBookHandler.handle(root);
                default -> log.trace("[Bithumb WS] 미처리 타입: {}", type);
            }
        } catch (Exception e) {
            log.error("[Bithumb WS] 메시지 처리 오류: {}", e.getMessage());
        }
    }

    @Override
    public void handleTransportError(org.springframework.web.socket.WebSocketSession session, Throwable ex) throws Exception {
        log.error("[Bithumb WS] 전송 오류: {}", ex.getMessage());
        scheduleReconnect();
    }

    @Override
    public void afterConnectionClosed(org.springframework.web.socket.WebSocketSession session, CloseStatus status) throws Exception {
        log.warn("[Bithumb WS] 연결 종료: {}", status);
        scheduleReconnect();
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // 구독
    private void subscribe(WebSocketSession session, String type) throws IOException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);
        payload.put("symbols", SYMBOLS);
        if ("ticker".equals(type)) {
            payload.put("tickTypes", List.of("24H"));
        }
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(payload)));
        log.info("[Bithumb WS] {} 구독 완료 ({}개 심볼)", type, SYMBOLS.size());
    }

    // 재연결
    private void scheduleReconnect() {
        int attempt = reconnectAttempts.incrementAndGet();

        long delay;
        if (attempt <= MAX_RECONNECT) {
            delay = Math.min(RECONNECT_BASE_MS * (1L << attempt), 30_000L);
            log.info("[Bithumb WS] {}ms 후 재연결 ({}/{})", delay, attempt, MAX_RECONNECT);
        } else {
            delay = RESET_AFTER_MS;
            log.warn("[Bithumb WS] 재연결 한도 초과 → {}분 후 카운터 리셋", RESET_AFTER_MS / 60_000);
        }

        Thread.startVirtualThread(() -> {
            try {
                Thread.sleep(Duration.ofMillis(delay));
                if (attempt > MAX_RECONNECT) {
                    reconnectAttempts.set(0);
                }
                connectInternal();
            } catch (InterruptedException ignore) {}
        });
    }
}
