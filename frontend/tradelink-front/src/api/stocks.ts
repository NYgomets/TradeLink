import client from './client'
import type { StockPriceSummaryDto, OrderBookDto, TradeHistoryDto, PageResponse } from '../types'

export const stocksApi = {
  getAll: () =>
    client.get<{ data: StockPriceSummaryDto[] }>('/stocks').then((r) => r.data.data),

  getPrice: (ticker: string) =>
    client.get<{ data: StockPriceSummaryDto }>(`/stocks/${ticker}/price`).then((r) => r.data.data),

  getOrderBook: (ticker: string) =>
    client.get<{ data: OrderBookDto }>(`/stocks/${ticker}/orderbook`).then((r) => r.data.data),

  // 주문 접수 (202 Accepted)
  placeOrder: (ticker: string, side: 'BUY' | 'SELL', quantity: number) =>
    client.post('/auth/stocks/orders', { ticker, side, quantity }),

  // 내 체결 내역
  getMyOrders: (cursorSeq?: number, size: number = 20) =>
  client
    .get<{ data: CursorPageResponse<TradeHistoryDto> }>('/auth/stocks/orders', {
      params: { cursorSeq, size }
    })
    .then((r) => r.data.data),
}
