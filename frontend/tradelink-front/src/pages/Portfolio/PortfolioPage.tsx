import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { portfolioApi } from '../../api/portfolio'
import { stocksApi } from '../../api/stocks'
import { useAuthStore } from '../../store/authStore'
import TotalAssetCard from '../../components/portfolio/TotalAssetCard'
import HoldingsList from '../../components/portfolio/HoldingsList'
import DepositModal from '../../components/portfolio/DepositModal'
import TradeHistory from '../../components/stocks/TradeHistory'
import type { TotalAssetDto, HoldingDto } from '../../types'

type Tab = 'holdings' | 'history'

export default function PortfolioPage() {
  const navigate = useNavigate()
  const { isLoggedIn, fetchMe } = useAuthStore()
  const [tab, setTab] = useState<Tab>('holdings')
  const [totalAsset, setTotalAsset] = useState<TotalAssetDto | null>(null)
  const [holdings, setHoldings] = useState<HoldingDto[]>([])
  const [prices, setPrices] = useState<Record<string, number>>({})
  const [loadingAsset, setLoadingAsset] = useState(true)
  const [loadingHoldings, setLoadingHoldings] = useState(true)
  const [showDeposit, setShowDeposit] = useState(false)

  const fetchAll = async () => {
    if (!isLoggedIn) return
    try {
      const [asset, holdings, stocks] = await Promise.all([
        portfolioApi.getTotalAsset(),
        portfolioApi.getHoldings(),
        stocksApi.getAll(),
      ])
      setTotalAsset(asset)
      setHoldings(holdings)
      // 종목 현재가 맵
      const priceMap: Record<string, number> = {}
      stocks.forEach((s) => { priceMap[s.ticker] = s.price })
      setPrices(priceMap)
    } catch {
    } finally {
      setLoadingAsset(false)
      setLoadingHoldings(false)
    }
  }

  useEffect(() => {
    fetchAll()
  }, [isLoggedIn])

  const handleDepositSuccess = () => {
    fetchAll()
    fetchMe() // 헤더 잔고도 갱신
  }

  if (!isLoggedIn) {
    return (
      <div className="flex flex-col items-center justify-center py-24 gap-4 animate-fade-up">
        <p className="text-text-muted text-sm">포트폴리오를 보려면 로그인이 필요합니다</p>
        <button
          onClick={() => navigate('/login')}
          className="px-5 py-2.5 bg-accent-cyan text-bg-primary rounded-xl text-sm font-semibold hover:brightness-110 transition-all"
        >
          로그인하기
        </button>
      </div>
    )
  }

  return (
    <div className="animate-fade-up">
      {/* 헤더 */}
      <div className="flex items-end justify-between mb-8">
        <div>
          <p className="text-xs font-mono uppercase tracking-widest text-accent-cyan mb-1">
            Portfolio
          </p>
          <h1 className="text-2xl font-semibold text-text-primary">내 포트폴리오</h1>
        </div>
        <button
          onClick={() => setShowDeposit(true)}
          className="flex items-center gap-2 px-4 py-2 bg-accent-cyan/10 hover:bg-accent-cyan/20 border border-accent-cyan/30 text-accent-cyan rounded-xl text-sm font-medium transition-all"
        >
          <span className="text-base leading-none">+</span>
          입금
        </button>
      </div>

      {/* 총 자산 카드 */}
      <div className="mb-4">
        <TotalAssetCard asset={totalAsset} loading={loadingAsset} />
      </div>

      {/* 탭 영역 */}
      <div className="bg-bg-card border border-border rounded-2xl overflow-hidden">
        <div className="flex border-b border-border">
          {([
            { key: 'holdings', label: '보유 종목' },
            { key: 'history', label: '체결 내역' },
          ] as { key: Tab; label: string }[]).map((t) => (
            <button
              key={t.key}
              onClick={() => setTab(t.key)}
              className={`flex-1 py-3.5 text-sm font-medium transition-all ${
                tab === t.key
                  ? 'text-accent-cyan border-b-2 border-accent-cyan -mb-px bg-accent-cyan/5'
                  : 'text-text-secondary hover:text-text-primary'
              }`}
            >
              {t.label}
              {t.key === 'holdings' && holdings.length > 0 && (
                <span className="ml-1.5 text-xs bg-accent-cyan/20 text-accent-cyan px-1.5 py-0.5 rounded-full font-mono">
                  {holdings.length}
                </span>
              )}
            </button>
          ))}
        </div>

        {tab === 'holdings' ? (
          <HoldingsList
            holdings={holdings}
            loading={loadingHoldings}
            prices={prices}
          />
        ) : (
          <div className="p-4">
            <TradeHistory />
          </div>
        )}
      </div>

      {/* 입금 모달 */}
      {showDeposit && (
        <DepositModal
          onSuccess={handleDepositSuccess}
          onClose={() => setShowDeposit(false)}
        />
      )}
    </div>
  )
}
