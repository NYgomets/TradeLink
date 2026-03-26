import { useState, useEffect } from 'react'
import { stocksApi } from '../../api/stocks'
import { useAuthStore } from '../../store/authStore'
import { useOrderSSE } from '../../hooks/useOrderSSE'
import type { StockPriceSummaryDto } from '../../types'

interface Props {
  ticker: string
  price: StockPriceSummaryDto | null
}

export default function OrderForm({ ticker, price }: Props) {
  const { member, isLoggedIn, fetchMe } = useAuthStore()
  const [side, setSide] = useState<'BUY' | 'SELL'>('BUY')
  const [quantityStr, setQuantityStr] = useState('1')
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null)

  const { latestOrder, clearLatest } = useOrderSSE()

  // ✅ SSE 체결 알림 수신 → 잔고 갱신 + 메시지 표시
  useEffect(() => {
    if (!latestOrder || latestOrder.ticker !== ticker) return

    fetchMe()

    if (latestOrder.status === 'FILLED') {
      setMessage({
        type: 'success',
        text: `${latestOrder.side === 'BUY' ? '매수' : '매도'} 체결 완료! ${latestOrder.price.toLocaleString()}원`,
      })
    } else {
      setMessage({ type: 'error', text: '체결에 실패했습니다. 다시 시도해주세요.' })
    }
    clearLatest()
  }, [latestOrder])

  const quantity = parseFloat(quantityStr) || 0
  const estimatedTotal = price ? price.price * quantity : 0

  const handleOrder = async () => {
    if (!isLoggedIn) {
      setMessage({ type: 'error', text: '로그인이 필요합니다' })
      return
    }
    if (quantity <= 0) {
      setMessage({ type: 'error', text: '수량을 입력해주세요' })
      return
    }

    setLoading(true)
    setMessage(null)
    try {
      await stocksApi.placeOrder(ticker, side, quantity)
      setMessage({ type: 'success', text: '주문 접수됨. 체결 알림을 기다려주세요.' })
      setQuantityStr('1')
      await fetchMe()
    } catch (e: any) {
      setMessage({
        type: 'error',
        text: e.response?.data?.message ?? '주문 접수에 실패했습니다',
      })
    } finally {
      setLoading(false)
    }
  }

  const handleQuantityChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const val = e.target.value
    if (val === '' || /^\d*\.?\d*$/.test(val)) {
      setQuantityStr(val)
    }
  }

  const step = 0.1

  return (
    <div className="flex flex-col gap-3">
      {/* 매수/매도 탭 */}
      <div className="grid grid-cols-2 gap-1 bg-bg-secondary rounded-lg p-1">
        {(['BUY', 'SELL'] as const).map((s) => (
          <button
            key={s}
            onClick={() => setSide(s)}
            className={`py-2 rounded-md text-sm font-medium transition-all ${
              side === s
                ? s === 'BUY'
                  ? 'bg-accent-green text-bg-primary'
                  : 'bg-accent-red text-white'
                : 'text-text-secondary hover:text-text-primary'
            }`}
          >
            {s === 'BUY' ? '매수' : '매도'}
          </button>
        ))}
      </div>

      {/* 현재가 */}
      <div className="bg-bg-secondary rounded-lg px-3 py-2 flex items-center justify-between">
        <span className="text-xs text-text-muted">시장가</span>
        <span className="font-mono text-sm text-text-primary">
          {price?.price.toLocaleString() ?? '-'}
          <span className="text-text-muted ml-1">원</span>
        </span>
      </div>

      {/* 수량 입력 */}
      <div>
        <label className="text-xs text-text-muted mb-1.5 block">
          수량 <span className="text-text-muted/60">(소수점 가능)</span>
        </label>
        <div className="flex items-center gap-2">
          <button
            onClick={() => {
              const next = Math.max(0.0001, Math.round((quantity - step) * 10000) / 10000)
              setQuantityStr(String(next))
            }}
            className="w-8 h-8 rounded border border-border text-text-secondary hover:text-text-primary hover:border-accent-cyan/40 transition-all font-mono"
          >
            -
          </button>
          <input
            type="text"
            inputMode="decimal"
            value={quantityStr}
            onChange={handleQuantityChange}
            placeholder="0.001"
            className="flex-1 bg-bg-secondary border border-border rounded-lg px-3 py-2 text-center font-mono text-sm text-text-primary focus:outline-none focus:border-accent-cyan/40"
          />
          <button
            onClick={() => {
              const next = Math.round((quantity + step) * 10000) / 10000
              setQuantityStr(String(next))
            }}
            className="w-8 h-8 rounded border border-border text-text-secondary hover:text-text-primary hover:border-accent-cyan/40 transition-all font-mono"
          >
            +
          </button>
        </div>
      </div>

      {/* 예상 금액 */}
      <div className="bg-bg-secondary rounded-lg px-3 py-2 flex items-center justify-between">
        <span className="text-xs text-text-muted">예상 금액</span>
        <span className="font-mono text-sm font-medium text-accent-cyan">
          {estimatedTotal.toLocaleString()}원
        </span>
      </div>

      {/* 가용 잔고 */}
      {member && (
        <div className="flex items-center justify-between px-1">
          <span className="text-xs text-text-muted">가용 잔고</span>
          <span className="font-mono text-xs text-text-secondary">
            {member.availableBalance.toLocaleString()}원
          </span>
        </div>
      )}

      {/* 주문 버튼 */}
      <button
        onClick={handleOrder}
        disabled={loading || !price || quantity <= 0}
        className={`w-full py-3 rounded-xl text-sm font-semibold transition-all disabled:opacity-40 disabled:cursor-not-allowed ${
          side === 'BUY'
            ? 'bg-accent-green text-bg-primary hover:brightness-110'
            : 'bg-accent-red text-white hover:brightness-110'
        }`}
      >
        {loading ? (
          <span className="flex items-center justify-center gap-2">
            <span className="w-4 h-4 border-2 border-current/30 border-t-current rounded-full animate-spin" />
            접수중...
          </span>
        ) : (
          `시장가 ${side === 'BUY' ? '매수' : '매도'}`
        )}
      </button>

      {/* 메시지 */}
      {message && (
        <div
          className={`text-xs px-3 py-2 rounded-lg animate-fade-up ${
            message.type === 'success'
              ? 'bg-accent-green/10 text-accent-green border border-accent-green/20'
              : 'bg-accent-red/10 text-accent-red border border-accent-red/20'
          }`}
        >
          {message.text}
        </div>
      )}

      {!isLoggedIn && (
        <p className="text-xs text-text-muted text-center">
          주문하려면{' '}
          <a href="/login" className="text-accent-cyan hover:underline">
            로그인
          </a>
          이 필요합니다
        </p>
      )}
    </div>
  )
}
