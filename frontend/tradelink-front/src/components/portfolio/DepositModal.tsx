import { useState } from 'react'
import { portfolioApi } from '../../api/portfolio'

interface Props {
  onSuccess: () => void
  onClose: () => void
}

const PRESETS = [1_000_000, 5_000_000, 10_000_000, 50_000_000]

export default function DepositModal({ onSuccess, onClose }: Props) {
  const [amount, setAmount] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const parsed = parseInt(amount.replace(/,/g, '')) || 0

  const handleDeposit = async () => {
    if (parsed <= 0 || parsed > 100_000_000) {
      setError('1원 이상 1억원 이하로 입력해주세요')
      return
    }
    setLoading(true)
    setError('')
    try {
      await portfolioApi.deposit(parsed)
      onSuccess()
      onClose()
    } catch (e: any) {
      setError(e.response?.data?.message ?? '입금에 실패했습니다')
    } finally {
      setLoading(false)
    }
  }

  const handleInput = (val: string) => {
    const num = val.replace(/[^0-9]/g, '')
    setAmount(num ? parseInt(num).toLocaleString() : '')
    setError('')
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* 배경 */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* 모달 */}
      <div className="relative bg-bg-card border border-border rounded-2xl p-6 w-full max-w-sm animate-fade-up shadow-2xl">
        <div className="flex items-center justify-between mb-5">
          <div>
            <p className="text-xs font-mono text-accent-cyan uppercase tracking-widest mb-0.5">
              Wallet
            </p>
            <h2 className="text-lg font-semibold text-text-primary">입금</h2>
          </div>
          <button
            onClick={onClose}
            className="text-text-muted hover:text-text-secondary transition-colors w-7 h-7 flex items-center justify-center rounded hover:bg-bg-hover"
          >
            ✕
          </button>
        </div>

        {/* 프리셋 버튼 */}
        <div className="grid grid-cols-2 gap-2 mb-4">
          {PRESETS.map((p) => (
            <button
              key={p}
              onClick={() => { setAmount(p.toLocaleString()); setError('') }}
              className="py-2 rounded-lg border border-border text-xs font-mono text-text-secondary hover:text-accent-cyan hover:border-accent-cyan/40 transition-all"
            >
              +{(p / 10000).toLocaleString()}만원
            </button>
          ))}
        </div>

        {/* 직접 입력 */}
        <div className="mb-1">
          <label className="text-xs text-text-muted mb-1.5 block">직접 입력</label>
          <div className="relative">
            <input
              type="text"
              value={amount}
              onChange={(e) => handleInput(e.target.value)}
              placeholder="0"
              className="w-full bg-bg-secondary border border-border rounded-xl px-4 py-3 font-mono text-right text-text-primary pr-10 focus:outline-none focus:border-accent-cyan/40 transition-colors"
            />
            <span className="absolute right-3 top-1/2 -translate-y-1/2 text-text-muted text-sm">
              원
            </span>
          </div>
        </div>

        {parsed > 0 && (
          <p className="text-xs text-text-muted text-right mb-3 font-mono">
            = {parsed.toLocaleString()}원
          </p>
        )}

        {error && (
          <p className="text-xs text-accent-red mb-3 bg-accent-red/10 px-3 py-2 rounded-lg border border-accent-red/20">
            {error}
          </p>
        )}

        <button
          onClick={handleDeposit}
          disabled={loading || parsed <= 0}
          className="w-full py-3 bg-accent-cyan text-bg-primary rounded-xl text-sm font-semibold hover:brightness-110 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
        >
          {loading ? (
            <span className="flex items-center justify-center gap-2">
              <span className="w-4 h-4 border-2 border-bg-primary/30 border-t-bg-primary rounded-full animate-spin" />
              처리중...
            </span>
          ) : (
            '입금하기'
          )}
        </button>
      </div>
    </div>
  )
}
