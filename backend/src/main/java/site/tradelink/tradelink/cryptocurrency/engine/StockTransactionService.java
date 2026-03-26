package site.tradelink.tradelink.cryptocurrency.engine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.tradelink.tradelink.cryptocurrency.entity.Holding;
import site.tradelink.tradelink.cryptocurrency.entity.OrderEvent;
import site.tradelink.tradelink.cryptocurrency.entity.TradeHistory;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.cryptocurrency.handler.CryptoName;
import site.tradelink.tradelink.cryptocurrency.repository.HoldingRepository;
import site.tradelink.tradelink.cryptocurrency.repository.TradeHistoryRepository;
import site.tradelink.tradelink.cryptocurrency.service.WalletService;
import site.tradelink.tradelink.supports.enums.ErrorCode;
import site.tradelink.tradelink.supports.exception.CustomException;

/**
 * 주식 체결 DB 트랜잭션 서비스
 * MatchingEngine에서 분리된 순수 DB 작업만 담당
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StockTransactionService {

    private final WalletService walletService;

    private final HoldingRepository holdingRepository;
    private final TradeHistoryRepository tradeHistoryRepository;

    // 체결 처리
    @Transactional
    public void process(OrderEvent event, long execPrice) {
        String ticker = event.getTicker();
        OrderSide side = event.getSide();
        Double quantity = event.getQuantity();
        Long memberSeq = event.getMemberSeq();
        long execAmount = (long) (execPrice * quantity);

        // 매도: 보유 수량 검증 + 차감
        if (OrderSide.SELL == side) {
            Holding holding = holdingRepository.findByMemberSeqAndTicker(memberSeq, ticker)
                    .orElseThrow(() -> new CustomException(ErrorCode.INSUFFICIENT_HOLDING));

            holding.sell(quantity);
            if (holding.getQuantity() == 0) {
                holdingRepository.delete(holding);
            }
            walletService.confirmSell(memberSeq, execAmount);
        }

        // 매수: Holding 평균가 갱신
        if (OrderSide.BUY == side) {
            walletService.confirmBuy(memberSeq, event.getReservedPrice(), execAmount);

            Holding holding = holdingRepository.findByMemberSeqAndTicker(memberSeq, ticker)
                    .orElseGet(() -> Holding.create(memberSeq, ticker, CryptoName.of(ticker)));

            holding.buy(execPrice, quantity);
            holdingRepository.save(holding);
        }

        // TradeHistory 저장
        tradeHistoryRepository.save(
                TradeHistory.of(memberSeq, event, execPrice, CryptoName.of(ticker)));
    }
}
