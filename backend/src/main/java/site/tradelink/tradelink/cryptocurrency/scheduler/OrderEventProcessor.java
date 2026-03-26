package site.tradelink.tradelink.cryptocurrency.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.tradelink.tradelink.cryptocurrency.engine.MatchingEngine;
import site.tradelink.tradelink.cryptocurrency.engine.ProcessorOffsetService;
import site.tradelink.tradelink.cryptocurrency.entity.OrderEvent;
import site.tradelink.tradelink.cryptocurrency.entity.ProcessorOffset;
import site.tradelink.tradelink.cryptocurrency.repository.OrderEventRepository;
import site.tradelink.tradelink.cryptocurrency.repository.ProcessorOffsetRepository;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * 주문 이벤트 프로세서
 *
 * ProcessorOffset에 등록된 ticker만 순회
 *
 * 실패해도 offset 전진 -> 다음 주문 블로킹 없음
 * OrderEvent 수정 없음 (순수 Append-Only 유지)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProcessor {

    private static final int BATCH_SIZE = 100;

    private final OrderEventRepository orderEventRepository;
    private final ProcessorOffsetRepository offsetRepository;
    private final MatchingEngine matchingEngine;
    private final ProcessorOffsetService processorOffsetService;

    @Scheduled(fixedDelayString = "${scheduler.order.process-delay-ms:500}")
    public void process() {
        List<ProcessorOffset> offsets = offsetRepository.findAll();
        if (offsets.isEmpty()) return;

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (ProcessorOffset offset : offsets) {
                executor.submit(() -> processTicker(offset.getTicker(), offset.getLastSeq()));
            }
        }
    }

    private void processTicker(String ticker, long lastSeq) {
        List<OrderEvent> events = orderEventRepository
                .findByTickerAfterSeq(ticker, lastSeq, BATCH_SIZE);

        if (events.isEmpty()) return;

        for (OrderEvent event : events) {
            try {
                matchingEngine.execute(event);
            } catch (Exception e) {
                log.warn("[Processor] {} seq={} 실패: {}", ticker, event.getSeq(), e.getMessage());
            }

            // 성공/실패 무관하게 offset 전진
            processorOffsetService.advanceOffset(ticker, event.getSeq());
        }
    }
}
