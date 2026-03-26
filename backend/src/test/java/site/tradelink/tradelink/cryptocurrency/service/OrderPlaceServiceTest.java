package site.tradelink.tradelink.cryptocurrency.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.tradelink.tradelink.cryptocurrency.dto.OrderRequestDto;
import site.tradelink.tradelink.cryptocurrency.entity.OrderEvent;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;
import site.tradelink.tradelink.cryptocurrency.repository.OrderEventRepository;

import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPlaceService 단위 테스트")
class OrderPlaceServiceTest {

    @InjectMocks OrderPlaceService orderPlaceService;
    @Mock OrderBookCache       orderBookCache;
    @Mock WalletService        walletService;
    @Mock OrderEventRepository orderEventRepository;

    private static final Long   MEMBER_SEQ = 1L;
    private static final String TICKER     = "BTC";
    private static final long   BEST_PRICE = 100_000_000L;

    @Nested
    @DisplayName("매수 주문 접수")
    class BuyOrder {

        @Test
        @DisplayName("정상 매수 주문 접수 시 reserve + save 호출")
        void success() {
            OrderRequestDto req = new OrderRequestDto(TICKER, OrderSide.BUY, 0.1);

            when(orderBookCache.getBestPrice(TICKER, OrderSide.BUY))
                    .thenReturn(OptionalLong.of(BEST_PRICE));
            when(orderEventRepository.save(any())).thenReturn(mock(OrderEvent.class));

            orderPlaceService.place(MEMBER_SEQ, req);

            long expectedReserved = (long) (BEST_PRICE * 0.1); // 10,000,000
            verify(walletService).reserve(MEMBER_SEQ, expectedReserved);
            verify(orderEventRepository).save(any(OrderEvent.class));
        }

        @Test
        @DisplayName("호가 stale 시 주문 거부 (reserve 미호출)")
        void staleOrderBook() {
            OrderRequestDto req = new OrderRequestDto(TICKER, OrderSide.BUY, 0.1);

            when(orderBookCache.getBestPrice(TICKER, OrderSide.BUY))
                    .thenReturn(OptionalLong.empty());

            assertThatThrownBy(() -> orderPlaceService.place(MEMBER_SEQ, req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("호가 데이터를 수신 중");

            verify(walletService, never()).reserve(any(), anyLong());
            verify(orderEventRepository, never()).save(any());
        }

        @Test
        @DisplayName("잔고 부족 시 OrderEvent INSERT 미호출")
        void insufficientBalance() {
            OrderRequestDto req = new OrderRequestDto(TICKER, OrderSide.BUY, 0.1);

            when(orderBookCache.getBestPrice(TICKER, OrderSide.BUY))
                    .thenReturn(OptionalLong.of(BEST_PRICE));
            doThrow(new IllegalStateException("주문 가능 금액이 부족합니다"))
                    .when(walletService).reserve(any(), anyLong());

            assertThatThrownBy(() -> orderPlaceService.place(MEMBER_SEQ, req))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("주문 가능 금액이 부족");

            verify(orderEventRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("매도 주문 접수")
    class SellOrder {

        @Test
        @DisplayName("매도 주문 시 reserve 미호출 (현금 예약 불필요)")
        void sellNoReserve() {
            OrderRequestDto req = new OrderRequestDto(TICKER, OrderSide.SELL, 0.1);

            when(orderBookCache.getBestPrice(TICKER, OrderSide.SELL))
                    .thenReturn(OptionalLong.of(BEST_PRICE));
            when(orderEventRepository.save(any())).thenReturn(mock(OrderEvent.class));

            orderPlaceService.place(MEMBER_SEQ, req);

            verify(walletService, never()).reserve(any(), anyLong());
            verify(orderEventRepository).save(any(OrderEvent.class));
        }
    }
}