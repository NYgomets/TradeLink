package site.tradelink.tradelink.cryptocurrency.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.cryptocurrency.disruptor.OrderEventDisruptorEngine;
import site.tradelink.tradelink.cryptocurrency.dto.OrderRequestDto;
import site.tradelink.tradelink.cryptocurrency.entity.OrderEvent;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;
import site.tradelink.tradelink.cryptocurrency.repository.OrderEventRepository;
import site.tradelink.tradelink.supports.enums.ErrorCode;
import site.tradelink.tradelink.supports.exception.CustomException;

/**
 * 주문 접수 퍼사드 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPlaceService {

    private final OrderBookCache orderBookCache;
    private final WalletService walletService;
    private final OrderEventDisruptorEngine disruptorEngine;

    @Transactional
    public void place(Long memberSeq, OrderRequestDto req) {

        if (memberSeq == null || req == null) {
            throw new CustomException(ErrorCode.WALLET_NOT_FOUND);
        }

        // 1. 호가 선검증
        long bestPrice = orderBookCache.getBestPrice(req.ticker(), req.side())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDERBOOK_STALE));

        // 2. 매수: 예약 차감 (availableBalance)
        long reservedAmount = 0L;
        if (OrderSide.BUY == req.side()) {
            reservedAmount = (long) (bestPrice * req.quantity());
            walletService.reserve(memberSeq, reservedAmount);
        }

        // 3. OrderEvent INSERT
        disruptorEngine.publish(
                memberSeq,
                req.ticker(),
                req.side(),
                req.quantity(),
                reservedAmount
        );
    }
}
