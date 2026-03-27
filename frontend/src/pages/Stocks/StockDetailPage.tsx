import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { stocksApi } from '../../api/stocks'
import { useStockSSE } from '../../hooks/useStockSSE'
import OrderBook from '../../components/stocks/OrderBook'
import OrderForm from '../../components/stocks/OrderForm'
import TradeHistory from '../../components/stocks/TradeHistory'
import type { StockPriceSummaryDto, OrderBookDto } from '../../types'

type Tab = 'orderbook' | 'history'

export default function StockDetailPage() {
  const { ticker } = useParams<{ ticker: string }>()
  const navigate = useNavigate()
  const [tab, setTab] = useState<Tab>('orderbook')

  // 초기 데이터
  const [initPrice, setInitPrice] = useState<StockPriceSummaryDto | null>(null)
  const [initOrderBook, setInitOrderBook] = useState<OrderBookDto | null>(null)
  const [loading, setLoading] = useState(true)

  // SSE 실시간 데이터
  const { price: ssePrice, orderBook: sseOrderBook, connected } = useStockSSE(ticker ?? null)

  // 최종 표시 데이터 (SSE 우선, 없으면 초기값)
  const price = ssePrice ?? initPrice
  const orderBook = sseOrderBook ?? initOrderBook

  // 가격 변동 flash
  const [prevPrice, setPrevPrice] = useState<number | null>(null)
  const [priceFlash, setPriceFlash] = useState<'up' | 'down' | null>(null)

  useEffect(() => {
    if (!ticker) return
    Promise.all([stocksApi.getPrice(ticker), stocksApi.getOrderBook(ticker)])
      .then(([p, ob]) => {
        setInitPrice(p)
        setInitOrderBook(ob)
      })
      .catch(() => navigate('/stocks'))
      .finally(() => setLoading(false))
  }, [ticker])

  useEffect(() => {
    if (!price) return
    if (prevPrice !== null && prevPrice !== price.price) {
      setPriceFlash(price.price > prevPrice ? 'up' : 'down')
      const t = setTimeout(() => setPriceFlash(null), 500)
      return () => clearTimeout(t)
    }
    setPrevPrice(price.price)
  }, [price?.price])

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <div className="w-6 h-6 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
      </div>
    )
  }

  const isUp = (price?.changePercent ?? 0) >= 0

  return (
    <div className="animate-fade-up">
      {/* 뒤로가기 */}
      <button
        onClick={() => navigate('/stocks')}
        className="flex items-center gap-1.5 text-sm text-text-muted hover:text-text-secondary mb-6 transition-colors"
      >
        <span>←</span> 종목 목록
      </button>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* 왼쪽: 가격 정보 + 호가/체결 내역 */}
        <div className="lg:col-span-2 flex flex-col gap-4">
          {/* 가격 카드 */}
          <div className="bg-bg-card border border-border rounded-xl p-5">
            <div className="flex items-start justify-between">
              <div>
                <div className="flex items-center gap-2 mb-1">
                  <h1 className="font-mono text-lg font-semibold text-text-primary">
                    {ticker}
                  </h1>
                  {price?.name && (
                    <span className="text-sm text-text-secondary">{price.name}</span>
                  )}
                  {/* SSE 연결 상태 */}
                  <span className={`w-1.5 h-1.5 rounded-full ${connected ? 'bg-accent-green animate-pulse-subtle' : 'bg-text-muted'}`} />
                </div>

                <div className="flex items-baseline gap-3">
                  <span
                    className={`font-mono text-3xl font-semibold transition-colors duration-300 ${
                      priceFlash === 'up'
                        ? 'text-accent-green'
                        : priceFlash === 'down'
                        ? 'text-accent-red'
                        : 'text-text-primary'
                    }`}
                  >
                    {price?.price.toLocaleString() ?? '-'}
                    <span className="text-sm text-text-muted ml-1">원</span>
                  </span>
                  <span
                    className={`font-mono text-sm font-medium ${
                      isUp ? 'text-accent-green' : 'text-accent-red'
                    }`}
                  >
                    {isUp ? '▲' : '▼'} {Math.abs(price?.changeAmount ?? 0).toLocaleString()} (
                    {price?.changePercent?.toFixed(2)}%)
                  </span>
                </div>
              </div>

              <div className="text-right">
                <div className="text-xs text-text-muted mb-1">거래량</div>
                <div className="font-mono text-sm text-text-secondary">
                  {price?.volume?.toLocaleString() ?? '-'}
                </div>
              </div>
            </div>
          </div>

          {/* 탭: 호가창 / 체결내역 */}
          <div className="bg-bg-card border border-border rounded-xl overflow-hidden">
            <div className="flex border-b border-border">
              {([
                { key: 'orderbook', label: '호가창' },
                { key: 'history', label: '내 체결내역' },
              ] as { key: Tab; label: string }[]).map((t) => (
                <button
                  key={t.key}
                  onClick={() => setTab(t.key)}
                  className={`flex-1 py-3 text-sm font-medium transition-all ${
                    tab === t.key
                      ? 'text-accent-cyan border-b-2 border-accent-cyan -mb-px bg-accent-cyan/5'
                      : 'text-text-secondary hover:text-text-primary'
                  }`}
                >
                  {t.label}
                </button>
              ))}
            </div>

            <div className="p-4">
              {tab === 'orderbook' ? (
                <OrderBook orderBook={orderBook} currentPrice={price?.price ?? null} />
              ) : (
                <TradeHistory />
              )}
            </div>
          </div>
        </div>

        {/* 오른쪽: 주문 패널 */}
        <div className="lg:col-span-1">
          <div className="bg-bg-card border border-border rounded-xl p-4 sticky top-20">
            <div className="text-xs font-mono text-text-secondary uppercase tracking-wider mb-4">
              주문하기
            </div>
            <OrderForm ticker={ticker!} price={price} />
          </div>
        </div>
      </div>
    </div>
  )
}
