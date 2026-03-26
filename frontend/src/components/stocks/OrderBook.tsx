import type { OrderBookDto } from '../../types'

interface Props {
  orderBook: OrderBookDto | null
  currentPrice: number | null
}

export default function OrderBook({ orderBook, currentPrice }: Props) {
  if (!orderBook) {
    return (
      <div className="flex items-center justify-center h-48 text-text-muted text-sm">
        호가 데이터 로딩중...
      </div>
    )
  }

  const maxQty = Math.max(
    ...orderBook.asks.map((a) => a.quantity),
    ...orderBook.bids.map((b) => b.quantity),
    1
  )

  return (
    <div className="font-mono text-xs">
      {/* 헤더 */}
      <div className="grid grid-cols-3 text-text-muted mb-1 px-2 py-1">
        <span>수량</span>
        <span className="text-center">가격</span>
        <span className="text-right">수량</span>
      </div>

      {/* 매도 호가 (역순 — 가장 낮은 매도가가 아래에) */}
      <div className="flex flex-col-reverse">
        {orderBook.asks.slice(0, 5).map((ask, i) => {
          const barWidth = (ask.quantity / maxQty) * 100
          return (
            <div key={i} className="relative grid grid-cols-3 items-center px-2 py-0.5 hover:bg-bg-hover transition-colors">
              <div
                className="absolute right-0 top-0 bottom-0 bg-accent-red/8 pointer-events-none"
                style={{ width: `${barWidth}%` }}
              />
              <span className="text-text-muted">{ask.quantity.toLocaleString()}</span>
              <span className="text-center text-accent-red font-medium">
                {ask.price.toLocaleString()}
              </span>
              <span className="text-right text-text-muted" />
            </div>
          )
        })}
      </div>

      {/* 현재가 구분선 */}
      <div className="border-y border-border-subtle my-1 py-1.5 px-2 bg-bg-secondary flex items-center justify-between">
        <span className="text-text-muted text-[10px] uppercase tracking-wider">현재가</span>
        <span className="text-text-primary font-semibold">
          {currentPrice?.toLocaleString() ?? '-'}
        </span>
        <span className="text-text-muted text-[10px] uppercase tracking-wider">KRW</span>
      </div>

      {/* 매수 호가 */}
      {orderBook.bids.slice(0, 5).map((bid, i) => {
        const barWidth = (bid.quantity / maxQty) * 100
        return (
          <div key={i} className="relative grid grid-cols-3 items-center px-2 py-0.5 hover:bg-bg-hover transition-colors">
            <div
              className="absolute left-0 top-0 bottom-0 bg-accent-green/8 pointer-events-none"
              style={{ width: `${barWidth}%` }}
            />
            <span className="text-text-muted text-right" />
            <span className="text-center text-accent-green font-medium">
              {bid.price.toLocaleString()}
            </span>
            <span className="text-right text-text-muted">{bid.quantity.toLocaleString()}</span>
          </div>
        )
      })}
    </div>
  )
}
