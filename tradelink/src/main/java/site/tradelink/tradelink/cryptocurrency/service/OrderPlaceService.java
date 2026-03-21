package site.tradelink.tradelink.cryptocurrency.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.cryptocurrency.dto.OrderRequestDto;
import site.tradelink.tradelink.cryptocurrency.entity.OrderEvent;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;
import site.tradelink.tradelink.cryptocurrency.repository.OrderEventRepository;

/**
 * 주문 접수 퍼사드 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPlaceService {

    private final OrderBookCache orderBookCache;
    private final WalletService        walletService;
    private final OrderEventRepository orderEventRepository;

    @Transactional
    public void place(Long memberSeq, OrderRequestDto req) {

        // 1. 호가 선검증 (stale 포함)
        long bestPrice = orderBookCache.getBestPrice(req.ticker(), req.side())
                .orElseThrow(() -> new IllegalStateException(
                        req.ticker() + " 호가 데이터를 수신 중입니다. 잠시 후 다시 시도해주세요."));

        // 2. 매수: 예약 차감 (availableBalance)
        long reservedAmount = 0L;
        if (OrderSide.BUY == req.side()) {
            reservedAmount = (long) (bestPrice * req.quantity());
            walletService.reserve(memberSeq, reservedAmount);
        }

        // 3. OrderEvent INSERT
        // reserve() 와 같은 트랜잭션 → save() 실패 시 reserve()도 롤백
        orderEventRepository.save(
                OrderEvent.create(memberSeq, req.ticker(), req.side(),
                        req.quantity(), reservedAmount)
        );
    }
}
