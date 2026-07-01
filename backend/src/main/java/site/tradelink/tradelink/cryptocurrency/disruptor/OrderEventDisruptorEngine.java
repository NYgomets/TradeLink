package site.tradelink.tradelink.cryptocurrency.disruptor;

import com.lmax.disruptor.*;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.cryptocurrency.handler.OrderMatchingHandler;
import site.tradelink.tradelink.cryptocurrency.handler.OrderPersistenceHandler;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderEventDisruptorEngine {

    private static final int RING_BUFFER_SIZE = 65536;

    private final OrderMatchingHandler matchingHandler;
    private final OrderPersistenceHandler persistenceHandler;

    private Disruptor<OrderEventModel> disruptor;
    private RingBuffer<OrderEventModel> ringBuffer;

    @PostConstruct
    public void init() {
        OrderEventModelFactory factory = new OrderEventModelFactory();

        // 디스럽터 인스턴스 초기화
        disruptor = new Disruptor<>(
                factory,
                RING_BUFFER_SIZE,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI, // 톰캣의 여러 API 요청 스레드가 병렬로 접근 가능하게 설정
                new BlockingWaitStrategy() // CPU 사용율을 안정적으로 유지하는 대기 전략
        );

        // 파이프라인 시퀀스 의존성 체인 정의
        // 1단계(인메모리 매칭)가 완전히 끝난 슬롯에 대해서만 2단계(영속화 및 후처리)가 뒤쫓아가며 실행됨을 보장
        disruptor.handleEventsWith(matchingHandler)
                .then(persistenceHandler);

        // 링버퍼 구동 및 시작
        ringBuffer = disruptor.start();
        log.info("[Disruptor Engine] 프리미티브 필드 기반 무중단 체결 파이프라인 허브 초기화 완료. 슬롯 크기: {}", RING_BUFFER_SIZE);
    }

    @PreDestroy
    public void shutdown() {
        if (disruptor != null) {
            disruptor.shutdown();
            log.info("[Disruptor Engine] 체결 파이프라인이 안전하게 종료되었습니다.");
        }
    }

    /**
     * 외부 진입점(OrderPlaceService)에서 호출할 락 프리 퍼블리싱 메서드
     * 힙(Heap)에 어떠한 임시 객체도 만들지 않고(Zero-Allocation), 프리미티브 값만 링버퍼 슬롯에 복사.
     */
    public void publish(long memberSeq, String ticker, OrderSide side, double quantity, long reservedAmount) {
        // 링버퍼에서 비어있는 다음 시퀀스 번호 획득
        long sequence = ringBuffer.next();
        try {
            // 해당 시퀀스의 미리 할당된 빈 모델 객체 추출
            OrderEventModel model = ringBuffer.get(sequence);

            // 데이터 쓰기 (오토박싱 발생 X)
            model.assignInput(memberSeq, ticker, side, quantity, reservedAmount);
        } finally {
            // 핸들러 스레드들이 읽어갈 수 있도록 시퀀스 오픈
            ringBuffer.publish(sequence);
        }
    }
}
