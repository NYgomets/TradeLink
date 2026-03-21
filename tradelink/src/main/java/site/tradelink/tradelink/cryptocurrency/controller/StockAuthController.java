package site.tradelink.tradelink.cryptocurrency.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import site.tradelink.tradelink.cryptocurrency.dto.OrderRequestDto;
import site.tradelink.tradelink.cryptocurrency.dto.TradeHistoryDto;
import site.tradelink.tradelink.cryptocurrency.entity.Holding;
import site.tradelink.tradelink.cryptocurrency.entity.OrderEvent;
import site.tradelink.tradelink.cryptocurrency.enums.OrderSide;
import site.tradelink.tradelink.cryptocurrency.inMemory.OrderBookCache;
import site.tradelink.tradelink.cryptocurrency.repository.HoldingRepository;
import site.tradelink.tradelink.cryptocurrency.repository.OrderEventRepository;
import site.tradelink.tradelink.cryptocurrency.repository.TradeHistoryRepository;
import site.tradelink.tradelink.cryptocurrency.service.OrderPlaceService;
import site.tradelink.tradelink.cryptocurrency.service.WalletService;
import site.tradelink.tradelink.cryptocurrency.sse.SseEmitterManager;
import site.tradelink.tradelink.supports.request.ApiResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auth/stocks")
@RequiredArgsConstructor
public class StockAuthController {

    private final OrderPlaceService orderPlaceService;
    private final TradeHistoryRepository tradeHistoryRepository;
    private final SseEmitterManager sseManager;

    // 주문 접수

    /**
     * 시장가 주문 접수
     * 1. 호가 존재 + stale 선검증
     * 2. 매수: bestPrice 기준 예약 차감 (슬리피지 대비)
     * 3. OrderEvent INSERT (Append-Only)
     * 4. 202 Accepted 즉시 반환
     */
    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<Void>> placeOrder(
            @AuthenticationPrincipal Long memberSeq,
            @Valid @RequestBody OrderRequestDto req) {

        orderPlaceService.place(memberSeq, req);
        return ResponseEntity.accepted().body(ApiResponse.ok(null));
    }

    // 내 체결 내역 조회

    /**
     * SSE 재연결 시 최근 체결 결과 확인용
     */
    @GetMapping("/orders")
    public ApiResponse<List<TradeHistoryDto>> getMyOrders(@AuthenticationPrincipal Long memberSeq) {
        return ApiResponse.ok(
                tradeHistoryRepository.findByMemberSeqOrderByCreateTimeDesc(memberSeq)
                        .stream()
                        .map(TradeHistoryDto::from)
                        .toList()
        );
    }

    // SSE 개인 알림 구독

    /**
     * 내 주문 알림 SSE 구독
     * 체결 시 my-order 이벤트 즉시 수신
     */
    @GetMapping(value = "/sse/orders", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToMyOrders(
            @AuthenticationPrincipal Long memberSeq,
            @RequestParam(required = false) String clientId) {

        if (clientId == null || clientId.isBlank()) clientId = UUID.randomUUID().toString();
        return sseManager.connectMember(memberSeq, clientId);
    }
}
