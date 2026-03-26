package site.tradelink.tradelink.cryptocurrency.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.cryptocurrency.entity.OrderEvent;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;
import site.tradelink.tradelink.cryptocurrency.service.WalletService;
import site.tradelink.tradelink.cryptocurrency.sse.SseEmitterManager;

import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MatchingEngine 단위 테스트")
class MatchingEngineTest {

    @InjectMocks MatchingEngine matchingEngine;
    @Mock OrderBookCache          orderBookCache;
    @Mock StockTransactionService transactionService;
    @Mock WalletService           walletService;
    @Mock SseEmitterManager       sseManager;

    private static final Long   MEMBER_SEQ      = 1L;
    private static final String TICKER          = "BTC";
    private static final long   EXEC_PRICE      = 100_000_000L;
    private static final long   RESERVED_AMOUNT = 10_000_000L;

    OrderEvent buyEvent;
    OrderEvent sellEvent;

    @BeforeEach
    void setUp() {
        buyEvent = OrderEvent.create(MEMBER_SEQ, TICKER, OrderSide.BUY, 0.1, RESERVED_AMOUNT);
        sellEvent = OrderEvent.create(MEMBER_SEQ, TICKER, OrderSide.SELL, 0.1, 0L);
    }

    @Nested
    @DisplayName("매수 체결")
    class BuyExecution {

        @Test
        @DisplayName("정상 체결 시 my-order FILLED SSE push")
        void success() {
            when(orderBookCache.getBestPrice(TICKER, OrderSide.BUY))
                    .thenReturn(OptionalLong.of(EXEC_PRICE));

            matchingEngine.execute(buyEvent);

            verify(transactionService).process(buyEvent, EXEC_PRICE);
            verify(sseManager).pushMyOrder(
                    eq(MEMBER_SEQ), eq(TICKER), eq(EXEC_PRICE),
                    anyDouble(), eq(OrderSide.BUY), eq("FILLED"), any());
        }

        @Test
        @DisplayName("슬리피지 실패 시 cancelReservation + FAILED SSE push + 예외 전파")
        void slippageFailure() {
            when(orderBookCache.getBestPrice(TICKER, OrderSide.BUY))
                    .thenReturn(OptionalLong.of(EXEC_PRICE));
            doThrow(new IllegalStateException("체결가 상승으로 잔고 부족"))
                    .when(transactionService).process(any(), anyLong());

            assertThatThrownBy(() -> matchingEngine.execute(buyEvent))
                    .isInstanceOf(IllegalStateException.class);

            // 예약금 환불 호출 확인
            verify(walletService).cancelReservation(MEMBER_SEQ, RESERVED_AMOUNT);
            // 실패 알림 SSE 호출 확인
            verify(sseManager).pushMyOrder(
                    eq(MEMBER_SEQ), eq(TICKER), eq(EXEC_PRICE),
                    anyDouble(), eq(OrderSide.BUY), eq("FAILED"), any());
        }

        @Test
        @DisplayName("호가 stale 시 예외 발생, transactionService 미호출")
        void staleOrderBook() {
            when(orderBookCache.getBestPrice(TICKER, OrderSide.BUY))
                    .thenReturn(OptionalLong.empty());

            assertThatThrownBy(() -> matchingEngine.execute(buyEvent))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("호가 없음 또는 stale");

            verify(transactionService, never()).process(any(), anyLong());
            verify(walletService, never()).cancelReservation(any(), anyLong());
        }
    }

    @Nested
    @DisplayName("매도 체결")
    class SellExecution {

        @Test
        @DisplayName("매도 체결 성공 시 FILLED SSE push")
        void success() {
            when(orderBookCache.getBestPrice(TICKER, OrderSide.SELL))
                    .thenReturn(OptionalLong.of(EXEC_PRICE));

            matchingEngine.execute(sellEvent);

            verify(transactionService).process(sellEvent, EXEC_PRICE);
            verify(sseManager).pushMyOrder(
                    eq(MEMBER_SEQ), eq(TICKER), eq(EXEC_PRICE),
                    anyDouble(), eq(OrderSide.SELL), eq("FILLED"), any());
        }

        @Test
        @DisplayName("매도 실패 시 cancelReservation 미호출 (매도는 예약금 없음)")
        void failureNoRefund() {
            when(orderBookCache.getBestPrice(TICKER, OrderSide.SELL))
                    .thenReturn(OptionalLong.of(EXEC_PRICE));
            doThrow(new IllegalStateException("보유 수량 없음"))
                    .when(transactionService).process(any(), anyLong());

            assertThatThrownBy(() -> matchingEngine.execute(sellEvent))
                    .isInstanceOf(IllegalStateException.class);

            verify(walletService, never()).cancelReservation(any(), anyLong());
        }
    }
}