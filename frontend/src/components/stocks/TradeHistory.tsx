import { useEffect, useState } from 'react'
import { stocksApi } from '../../api/stocks'
import { useAuthStore } from '../../store/authStore'
import { useOrderSSE, toTradeHistoryDto } from '../../hooks/useOrderSSE'
import type { TradeHistoryDto } from '../../types'

export default function TradeHistory() {
  const { isLoggedIn } = useAuthStore()
  const [history, setHistory] = useState<TradeHistoryDto[]>([])
  const [nextCursor, setNextCursor] = useState<number | null>(null)
  const [hasNext, setHasNext] = useState(false)
  const [loading, setLoading] = useState(false)
  const { latestOrder, clearLatest } = useOrderSSE()

  const fetchOrders = async (cursor?: number) => {
    if (loading) return
    setLoading(true)
    try {
      const response = await stocksApi.getMyOrders(cursor, 20)
      if (!cursor) {
        setHistory(response.content)
      } else {
        setHistory((prev) => [...prev, ...response.content])
      }
      setNextCursor(response.nextCursor)
      setHasNext(response.hasNext)
    } catch (e) {
      console.error(e)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (!isLoggedIn) return
    fetchOrders()
  }, [isLoggedIn])

  useEffect(() => {
    if (!latestOrder) return
    if (latestOrder.status === 'FAILED') {
      clearLatest()
      return
    }
    setHistory((prev) => [toTradeHistoryDto(latestOrder), ...prev])
    clearLatest()
  }, [latestOrder])

  if (!isLoggedIn) {
    return (
      <div className="text-center py-8 text-text-muted text-sm">
        로그인 후 체결 내역을 확인할 수 있어요
      </div>
    )
  }

  if (!history.length && !loading) {
    return (
      <div className="text-center py-8 text-text-muted text-sm">체결 내역이 없습니다</div>
    )
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-xs font-mono">
        <thead>
          <tr className="border-b border-border text-text-muted uppercase tracking-wider text-left">
            <th className="px-3 py-2">종목</th>
            <th className="px-3 py-2 text-center">종류</th>
            <th className="px-3 py-2 text-right">체결가</th>
            <th className="px-3 py-2 text-right">수량</th>
            <th className="px-3 py-2 text-right">총액</th>
            <th className="px-3 py-2 text-right">시간</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-border-subtle">
          {history.map((h, i) => (
            <tr key={h.seq || i} className="hover:bg-bg-hover transition-colors">
              <td className="px-3 py-2 text-text-primary font-medium">{h.ticker}</td>
              <td className="px-3 py-2 text-center">
                <span className={`px-1.5 py-0.5 rounded text-[10px] font-semibold ${
                  h.side === 'BUY' ? 'bg-accent-green/15 text-accent-green' : 'bg-accent-red/15 text-accent-red'
                }`}>
                  {h.side === 'BUY' ? '매수' : '매도'}
                </span>
              </td>
              <td className="px-3 py-2 text-right text-text-primary">{h.execPrice.toLocaleString()}원</td>
              <td className="px-3 py-2 text-right text-text-secondary">{h.quantity}</td>
              <td className="px-3 py-2 text-right text-text-secondary">{h.totalAmount.toLocaleString()}원</td>
              <td className="px-3 py-2 text-right text-text-muted">
                {h.tradedAt ? h.tradedAt.replace('T', ' ').slice(0, 19) : '-'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {hasNext && (
        <div className="flex justify-center mt-6 mb-2">
          <button
            onClick={() => fetchOrders(nextCursor!)}
            disabled={loading}
            className="px-6 py-2 bg-bg-secondary border border-border rounded-xl text-sm font-medium text-text-secondary hover:text-text-primary hover:border-accent-cyan/40 transition-all disabled:opacity-50"
          >
            {loading ? '불러오는 중...' : '더보기 ▼'}
          </button>
        </div>
      )}
    </div>
  )
}