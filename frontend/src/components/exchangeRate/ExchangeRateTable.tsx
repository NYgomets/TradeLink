import type { ExchangeRateSummaryDto } from '../../types'

interface Props {
  data: ExchangeRateSummaryDto[]
  loading: boolean
}

export default function ExchangeRateTable({ data, loading }: Props) {
  if (loading) {
    return (
      <div className="p-8 flex justify-center">
        <div className="w-5 h-5 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
      </div>
    )
  }

  if (!data.length) {
    return (
      <div className="p-8 text-center text-text-muted text-sm">데이터가 없습니다</div>
    )
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="text-xs text-text-muted font-mono uppercase tracking-wider">
            <th className="px-5 py-2.5 text-left">날짜</th>
            <th className="px-5 py-2.5 text-right">기준가</th>
            <th className="px-5 py-2.5 text-right">변동액</th>
            <th className="px-5 py-2.5 text-right">등락률</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-border-subtle">
          {data.map((row, i) => {
            // ✅ changeAmount 기준으로 등락 판단
            const isUp = (row.changeAmount ?? 0) >= 0
            return (
              <tr
                key={`${row.baseDateTime}-${i}`}
                className="hover:bg-bg-hover transition-colors"
              >
                {/* ✅ date → baseDateTime */}
                <td className="px-5 py-2.5 font-mono text-xs text-text-secondary">
                  {row.baseDateTime?.slice(0, 10) ?? '-'}
                </td>
                {/* ✅ basePrice → rate */}
                <td className="px-5 py-2.5 font-mono text-right text-text-primary">
                  {row.rate?.toLocaleString() ?? '-'}
                </td>
                {/* ✅ changePrice → changeAmount */}
                <td className={`px-5 py-2.5 font-mono text-right text-xs ${isUp ? 'text-accent-green' : 'text-accent-red'}`}>
                  {isUp ? '+' : ''}{row.changeAmount?.toLocaleString() ?? '-'}
                </td>
                {/* ✅ changePercentage → changePercent */}
                <td className={`px-5 py-2.5 font-mono text-right text-xs ${isUp ? 'text-accent-green' : 'text-accent-red'}`}>
                  {isUp ? '+' : ''}{row.changePercent?.toFixed(2) ?? '-'}%
                </td>
              </tr>
            )
          })}
        </tbody>
      </table>
    </div>
  )
}
