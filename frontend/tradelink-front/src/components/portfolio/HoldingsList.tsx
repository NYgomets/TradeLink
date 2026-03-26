import { useNavigate } from 'react-router-dom'
import type { HoldingDto } from '../../types'

interface Props {
  holdings: HoldingDto[]
  loading: boolean
  prices: Record<string, number>
}

export default function HoldingsList({ holdings, loading, prices }: Props) {
  const navigate = useNavigate()

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <div className="w-5 h-5 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
      </div>
    )
  }

  if (!holdings.length) {
    return (
      <div className="py-10 text-center">
        <p className="text-text-muted text-sm">보유 종목이 없습니다</p>
        <button
          onClick={() => navigate('/stocks')}
          className="mt-3 text-xs text-accent-cyan hover:underline"
        >
          종목 보러가기 →
        </button>
      </div>
    )
  }

  return (
    <div className="divide-y divide-border-subtle">
      {holdings.map((h, i) => {
        const currentPrice = prices[h.ticker] ?? h.avgPrice
        const evalValue = currentPrice * h.quantity
        const profitAbs = (currentPrice - h.avgPrice) * h.quantity
        const profitRate = h.avgPrice > 0 ? ((currentPrice - h.avgPrice) / h.avgPrice) * 100 : 0
        const isProfit = profitAbs >= 0

        return (
          <div
            key={h.ticker}
            onClick={() => navigate(`/stocks/${h.ticker}`)}
            className="flex items-center justify-between px-5 py-4 hover:bg-bg-hover cursor-pointer transition-colors animate-fade-up"
            style={{ animationDelay: `${i * 40}ms` }}
          >
            <div className="flex items-center gap-3">
              <div className="w-8 h-8 rounded-lg bg-bg-secondary border border-border flex items-center justify-center">
                <span className="font-mono text-xs text-text-secondary">
                  {h.ticker.slice(0, 2)}
                </span>
              </div>
              <div>
                <div className="font-mono text-sm font-medium text-text-primary">
                  {h.ticker}
                </div>
                <div className="text-xs text-text-muted mt-0.5">
                  {h.quantity.toLocaleString()}주 · 평균 {h.avgPrice.toLocaleString()}원
                </div>
              </div>
            </div>

            <div className="text-right">
              <div className="font-mono text-sm text-text-primary">
                {evalValue.toLocaleString()}원
              </div>
              <div className={`font-mono text-xs ${isProfit ? 'text-accent-green' : 'text-accent-red'}`}>
                {isProfit ? '+' : ''}{profitAbs.toLocaleString()}원 ({isProfit ? '+' : ''}{profitRate.toFixed(2)}%)
              </div>
            </div>
          </div>
        )
      })}
    </div>
  )
}
