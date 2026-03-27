import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { stocksApi } from '../../api/stocks'
import type { StockPriceSummaryDto } from '../../types'

export default function StocksPage() {
  const navigate = useNavigate()
  const [stocks, setStocks] = useState<StockPriceSummaryDto[]>([])
  const [loading, setLoading] = useState(true)
  const [prevPrices, setPrevPrices] = useState<Record<string, number>>({})
  const [flashing, setFlashing] = useState<Record<string, 'up' | 'down'>>({})

  useEffect(() => {
    const fetchStocks = async () => {
      try {
        const data = await stocksApi.getAll()

        setFlashing(() => {
          const next: Record<string, 'up' | 'down'> = {}
          data.forEach((s) => {
            const old = prevPrices[s.ticker]
            if (old !== undefined && old !== s.price) {
              next[s.ticker] = s.price > old ? 'up' : 'down'
            }
          })
          return next
        })

        setPrevPrices(() => {
          const next: Record<string, number> = {}
          data.forEach((s) => { next[s.ticker] = s.price })
          return next
        })

        setStocks(data)
      } catch {
      } finally {
        setLoading(false)
      }
    }

    fetchStocks()
    const id = setInterval(fetchStocks, 5000)
    return () => clearInterval(id)
  }, [])

  useEffect(() => {
    if (Object.keys(flashing).length === 0) return
    const t = setTimeout(() => setFlashing({}), 600)
    return () => clearTimeout(t)
  }, [flashing])

  return (
    <div className="animate-fade-up">
      <div className="mb-8">
        <p className="text-xs font-mono uppercase tracking-widest text-accent-cyan mb-1">
          Simulated Market
        </p>
        <h1 className="text-2xl font-semibold text-text-primary">모의투자</h1>
        <p className="text-sm text-text-secondary mt-1">
          종목을 선택해 실시간 시세와 주문을 확인하세요
        </p>
      </div>

      {loading ? (
        <div className="flex justify-center py-20">
          <div className="w-6 h-6 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
          {stocks.map((stock) => {
            // ✅ 수정 1: animationDelay 제거 → 카드 전체가 즉시 렌더링됨
            const isUp = stock.changePercent >= 0
            const flash = flashing[stock.ticker]
            return (
              <div
                key={stock.ticker}
                onClick={() => navigate(`/stocks/${stock.ticker}`)}
                className={`bg-bg-card border rounded-xl p-4 cursor-pointer transition-all duration-200 hover:border-accent-cyan/30 hover:bg-bg-hover animate-fade-up ${
                  flash === 'up'
                    ? 'border-accent-green/60 bg-accent-green/5'
                    : flash === 'down'
                    ? 'border-accent-red/60 bg-accent-red/5'
                    : 'border-border'
                }`}
              >
                <div className="flex items-start justify-between mb-3">
                  <div>
                    <div className="font-mono text-sm font-semibold text-text-primary">
                      {stock.ticker}
                    </div>
                    <div className="text-xs text-text-muted mt-0.5 truncate max-w-[120px]">
                      {stock.name}
                    </div>
                  </div>
                  <span
                    className={`text-xs px-1.5 py-0.5 rounded font-mono font-medium ${
                      isUp
                        ? 'bg-accent-green/15 text-accent-green'
                        : 'bg-accent-red/15 text-accent-red'
                    }`}
                  >
                    {isUp ? '+' : ''}{stock.changePercent?.toFixed(2)}%
                  </span>
                </div>

                <div className="font-mono text-xl font-semibold text-text-primary">
                  {stock.price.toLocaleString()}
                  <span className="text-xs text-text-muted ml-1">원</span>
                </div>

                <div className="flex items-center justify-between mt-2">
                  <span className={`font-mono text-xs ${isUp ? 'text-accent-green' : 'text-accent-red'}`}>
                    {isUp ? '▲' : '▼'} {Math.abs(stock.changeAmount).toLocaleString()}
                  </span>
                  <span className="text-xs text-text-muted">
                    거래량 {stock.volume?.toLocaleString() ?? '-'}
                  </span>
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
