import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts'
import type { ExchangeRateChartPointDto } from '../../types'

interface Props {
  data: ExchangeRateChartPointDto[]
}

const CustomTooltip = ({ active, payload, label }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className="bg-bg-card border border-border rounded-lg px-3 py-2 shadow-xl">
        <p className="text-xs text-text-muted mb-1">{label}</p>
        <p className="font-mono text-sm font-medium text-accent-cyan">
          {payload[0].value?.toLocaleString()}원
        </p>
      </div>
    )
  }
  return null
}

export default function ExchangeRateChart({ data }: Props) {
  if (!data.length) {
    return (
      <div className="h-48 flex items-center justify-center text-text-muted text-sm">
        데이터가 없습니다
      </div>
    )
  }

  // ✅ basePrice → rate
  const prices = data.map((d) => d.rate)
  const min = Math.min(...prices)
  const max = Math.max(...prices)
  const padding = (max - min) * 0.1

  // ✅ date → baseDateTime (슬라이싱으로 날짜만 표시)
  const chartData = data.map((d) => ({
    date: d.baseDateTime?.slice(0, 10) ?? '',
    rate: d.rate,
  }))

  return (
    <ResponsiveContainer width="100%" height={200}>
      <AreaChart data={chartData} margin={{ top: 4, right: 4, left: 0, bottom: 0 }}>
        <defs>
          <linearGradient id="chartGradient" x1="0" y1="0" x2="0" y2="1">
            <stop offset="5%" stopColor="#00d4ff" stopOpacity={0.15} />
            <stop offset="95%" stopColor="#00d4ff" stopOpacity={0} />
          </linearGradient>
        </defs>
        <CartesianGrid strokeDasharray="3 3" stroke="#1e2433" vertical={false} />
        <XAxis
          dataKey="date"
          tick={{ fill: '#4a5168', fontSize: 10, fontFamily: 'JetBrains Mono' }}
          axisLine={false}
          tickLine={false}
          interval="preserveStartEnd"
        />
        <YAxis
          domain={[min - padding, max + padding]}
          tick={{ fill: '#4a5168', fontSize: 10, fontFamily: 'JetBrains Mono' }}
          axisLine={false}
          tickLine={false}
          tickFormatter={(v) => v.toLocaleString()}
          width={70}
        />
        <Tooltip content={<CustomTooltip />} />
        <Area
          type="monotone"
          dataKey="rate"
          stroke="#00d4ff"
          strokeWidth={1.5}
          fill="url(#chartGradient)"
          dot={false}
          activeDot={{ r: 4, fill: '#00d4ff', stroke: '#0a0c10', strokeWidth: 2 }}
        />
      </AreaChart>
    </ResponsiveContainer>
  )
}
