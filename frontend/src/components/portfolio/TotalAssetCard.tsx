import type { TotalAssetDto } from '../../types'

interface Props {
  asset: TotalAssetDto | null
  loading: boolean
}

function StatItem({
  label,
  value,
  accent,
}: {
  label: string
  value: string
  accent?: boolean
}) {
  return (
    <div className="bg-bg-secondary rounded-xl px-4 py-3">
      <div className="text-xs text-text-muted mb-1">{label}</div>
      <div className={`font-mono text-sm font-medium ${accent ? 'text-accent-cyan' : 'text-text-primary'}`}>
        {value}
      </div>
    </div>
  )
}

export default function TotalAssetCard({ asset, loading }: Props) {
  if (loading) {
    return (
      <div className="bg-bg-card border border-border rounded-2xl p-6 flex items-center justify-center h-44">
        <div className="w-5 h-5 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
      </div>
    )
  }

  if (!asset) {
    return (
      <div className="bg-bg-card border border-border rounded-2xl p-6 flex items-center justify-center h-44 text-text-muted text-sm">
        로그인 후 확인할 수 있어요
      </div>
    )
  }

  const holdingRatio =
    asset.totalAsset > 0 ? (asset.holdingValue / asset.totalAsset) * 100 : 0
  const cashRatio = 100 - holdingRatio

  return (
    <div className="bg-bg-card border border-border rounded-2xl p-6 animate-fade-up">
      {/* 총 자산 */}
      <div className="mb-5">
        <p className="text-xs font-mono uppercase tracking-widest text-accent-cyan mb-1">
          Total Asset
        </p>
        <div className="flex items-baseline gap-2">
          <span className="font-mono text-3xl font-semibold text-text-primary">
            {asset.totalAsset.toLocaleString()}
          </span>
          <span className="text-sm text-text-muted">원</span>
        </div>
      </div>

      {/* 자산 구성 바 */}
      <div className="mb-4">
        <div className="flex h-2 rounded-full overflow-hidden gap-0.5">
          <div
            className="bg-accent-cyan transition-all duration-700 rounded-l-full"
            style={{ width: `${cashRatio}%` }}
          />
          <div
            className="bg-accent-amber transition-all duration-700 rounded-r-full"
            style={{ width: `${holdingRatio}%` }}
          />
        </div>
        <div className="flex items-center gap-4 mt-1.5">
          <span className="flex items-center gap-1 text-[10px] text-text-muted">
            <span className="w-1.5 h-1.5 rounded-full bg-accent-cyan inline-block" />
            현금 {cashRatio.toFixed(1)}%
          </span>
          <span className="flex items-center gap-1 text-[10px] text-text-muted">
            <span className="w-1.5 h-1.5 rounded-full bg-accent-amber inline-block" />
            종목 {holdingRatio.toFixed(1)}%
          </span>
        </div>
      </div>

      {/* 세부 */}
      <div className="grid grid-cols-3 gap-2">
        <StatItem label="현금 잔고" value={`${asset.balance.toLocaleString()}원`} />
        <StatItem
          label="가용 잔고"
          value={`${asset.availableBalance.toLocaleString()}원`}
          accent
        />
        <StatItem label="종목 평가액" value={`${asset.holdingValue.toLocaleString()}원`} />
      </div>
    </div>
  )
}
