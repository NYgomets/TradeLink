import { useEffect, useState, useCallback } from 'react'
import { exchangeRateApi } from '../../api/exchangeRate'
import type { ExchangeRateSummaryDto, ExchangeRateChartPointDto, Period } from '../../types'
import ExchangeRateChart from '../../components/exchangeRate/ExchangeRateChart'
import ExchangeRateTable from '../../components/exchangeRate/ExchangeRateTable'

// ✅ 1D 추가
const PERIODS: { label: string; value: Period }[] = [
  { label: '당일', value: '1D' },
  { label: '1주', value: '1W' },
  { label: '1개월', value: '1M' },
  { label: '3개월', value: '3M' },
  { label: '6개월', value: '6M' },
  { label: '1년', value: '1Y' },
]

const CURRENCY_FLAGS: Record<string, string> = {
  USD: '🇺🇸',
  EUR: '🇪🇺',
  JPY: '🇯🇵',
  CNY: '🇨🇳',
  GBP: '🇬🇧',
  AUD: '🇦🇺',
  CAD: '🇨🇦',
  CHF: '🇨🇭',
  HKD: '🇭🇰',
  SGD: '🇸🇬',
}

export default function ExchangeRatePage() {
  const [rates, setRates] = useState<ExchangeRateSummaryDto[]>([])
  const [selected, setSelected] = useState<ExchangeRateSummaryDto | null>(null)
  const [period, setPeriod] = useState<Period>('1M')
  const [chartData, setChartData] = useState<ExchangeRateChartPointDto[]>([])
  const [tableData, setTableData] = useState<ExchangeRateSummaryDto[]>([])
  const [loadingRates, setLoadingRates] = useState(true)
  const [loadingDetail, setLoadingDetail] = useState(false)

  useEffect(() => {
    const fetch = async () => {
      try {
        const data = await exchangeRateApi.getLatest()
        setRates(data)
        setSelected(prev => {
          if (!prev) return data[0] ?? null
          // 선택된 통화의 최신 데이터로 업데이트
          return data.find(r => r.currencyCode === prev.currencyCode) ?? prev
        })
      } catch {
      } finally {
        setLoadingRates(false)
      }
    }
    fetch()
    const id = setInterval(fetch, 5000)
    return () => clearInterval(id)
  }, [])

  const fetchDetail = useCallback(async () => {
    if (!selected) return
    setLoadingDetail(true)
    try {
      const [chart, table] = await Promise.all([
        exchangeRateApi.getChart(selected.currencyCode, period),
        exchangeRateApi.getTable(selected.currencyCode, period),
      ])
      setChartData(chart)
      setTableData(table)
    } catch {
    } finally {
      setLoadingDetail(false)
    }
  }, [selected?.currencyCode, period])

  useEffect(() => {
    fetchDetail()
  }, [fetchDetail])

  return (
    <div className="animate-fade-up">
      <div className="mb-8">
        <p className="text-xs font-mono uppercase tracking-widest text-accent-cyan mb-1">
          Foreign Exchange
        </p>
        <h1 className="text-2xl font-semibold text-text-primary">실시간 환율</h1>
        <p className="text-sm text-text-secondary mt-1">5초마다 자동 갱신됩니다</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
        {/* 왼쪽: 통화 목록 */}
        <div className="lg:col-span-1">
          <div className="bg-bg-card border border-border rounded-xl overflow-hidden">
            <div className="px-4 py-3 border-b border-border">
              <span className="text-xs font-mono text-text-secondary uppercase tracking-wider">
                통화 목록
              </span>
            </div>
            {loadingRates ? (
              <div className="p-8 flex items-center justify-center">
                <div className="w-5 h-5 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
              </div>
            ) : (
              <ul className="divide-y divide-border-subtle">
                {rates.map((rate, i) => {
                  const isUp = (rate.changeAmount ?? 0) >= 0
                  const isSelected = selected?.currencyCode === rate.currencyCode
                  return (
                    <li
                      key={rate.currencyCode}
                      onClick={() => setSelected(rate)}
                      className={`px-4 py-3 cursor-pointer transition-all duration-150 ${
                        isSelected
                          ? 'bg-accent-cyan/5 border-l-2 border-l-accent-cyan'
                          : 'hover:bg-bg-hover border-l-2 border-l-transparent'
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2.5">
                          <span className="text-lg leading-none">
                            {CURRENCY_FLAGS[rate.currencyCode] ?? '💱'}
                          </span>
                          <div>
                            <div className="text-sm font-medium text-text-primary">
                              {rate.currencyCode}
                            </div>
                            <div className="text-xs text-text-muted truncate max-w-[100px]">
                              {rate.currencyName}
                            </div>
                          </div>
                        </div>
                        <div className="text-right">
                          <div className="font-mono text-sm font-medium text-text-primary">
                            {rate.rate.toLocaleString()}
                          </div>
                          <div className={`font-mono text-xs ${isUp ? 'text-accent-green' : 'text-accent-red'}`}>
                            {isUp ? '+' : ''}{rate.changePercent?.toFixed(2)}%
                          </div>
                        </div>
                      </div>
                    </li>
                  )
                })}
              </ul>
            )}
          </div>
        </div>

        {/* 오른쪽: 상세 차트 + 테이블 */}
        <div className="lg:col-span-2 flex flex-col gap-4">
          {selected && (
            <>
              {/* 상단 요약 카드 */}
              <div className="bg-bg-card border border-border rounded-xl p-5">
                <div className="flex items-start justify-between mb-4">
                  <div>
                    <div className="flex items-center gap-2 mb-1">
                      <span className="text-xl">
                        {CURRENCY_FLAGS[selected.currencyCode] ?? '💱'}
                      </span>
                      <h2 className="text-lg font-semibold">{selected.currencyCode}</h2>
                      <span className="text-sm text-text-secondary">{selected.currencyName}</span>
                    </div>
                    <div className="flex items-baseline gap-3">
                      <span className="font-mono text-3xl font-semibold text-text-primary">
                        {selected.rate.toLocaleString()}
                        <span className="text-sm text-text-muted ml-1">원</span>
                      </span>
                      <span className={`font-mono text-sm font-medium ${
                        (selected.changeAmount ?? 0) >= 0 ? 'text-accent-green' : 'text-accent-red'
                      }`}>
                        {(selected.changeAmount ?? 0) >= 0 ? '▲' : '▼'}{' '}
                        {Math.abs(selected.changeAmount ?? 0).toLocaleString()} (
                        {selected.changePercent?.toFixed(2)}%)
                      </span>
                    </div>
                  </div>

                  {/* 기간 선택 */}
                  <div className="flex gap-1 flex-wrap justify-end">
                    {PERIODS.map((p) => (
                      <button
                        key={p.value}
                        onClick={() => setPeriod(p.value)}
                        className={`px-2.5 py-1 rounded text-xs font-mono font-medium transition-all ${
                          period === p.value
                            ? 'bg-accent-cyan text-bg-primary'
                            : 'text-text-secondary hover:text-text-primary hover:bg-bg-hover'
                        }`}
                      >
                        {p.label}
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              {/* 차트 */}
              <div className="bg-bg-card border border-border rounded-xl p-5">
                <div className="flex items-center justify-between mb-4">
                  <span className="text-xs font-mono text-text-secondary uppercase tracking-wider">
                    환율 추이
                  </span>
                  {loadingDetail && (
                    <div className="w-4 h-4 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
                  )}
                </div>
                <ExchangeRateChart data={chartData} />
              </div>

              {/* 테이블 - 당일 선택 시 상세 로그라 테이블 의미 없으므로 숨김 */}
              {period !== '1D' && (
                <div className="bg-bg-card border border-border rounded-xl overflow-hidden">
                  <div className="px-5 py-3 border-b border-border">
                    <span className="text-xs font-mono text-text-secondary uppercase tracking-wider">
                      기간별 데이터
                    </span>
                  </div>
                  <ExchangeRateTable data={tableData} loading={loadingDetail} />
                </div>
              )}
            </>
          )}
        </div>
      </div>
    </div>
  )
}
