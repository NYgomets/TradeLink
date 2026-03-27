import { useEffect, useState } from 'react'
import type { TradeHistoryDto } from '../types'
import { useAuthStore } from '../store/authStore'

// 백엔드 MyOrderDto와 일치
export interface MyOrderSseDto {
  ticker: string
  price: number        // MyOrderDto.price (long)
  quantity: number
  side: 'BUY' | 'SELL'
  status: string       // FILLED | FAILED
  at: string           // LocalDateTime → JSON string
}

// MyOrderSseDto → TradeHistoryDto 변환
export function toTradeHistoryDto(order: MyOrderSseDto): TradeHistoryDto {
  return {
    seq: 0,                          // SSE에는 seq 없음
    ticker: order.ticker,
    name: order.ticker,              // SSE에는 name 없음, ticker로 대체
    side: order.side,
    quantity: order.quantity,
    execPrice: order.price,          // ✅ price → execPrice
    totalAmount: order.price * order.quantity,
    tradedAt: order.at,              // ✅ at → tradedAt
  }
}

// ─── 싱글턴 SSE 관리 ───────────────────────────────────────────
// OrderForm과 TradeHistory 각각에서 호출해도 연결 1개만 유지
let listeners: Array<(order: MyOrderSseDto) => void> = []
let esInstance: EventSource | null = null

function subscribeSSE() {
  if (esInstance) return

  const clientId = `order-${Date.now()}`
  const url = `${import.meta.env.VITE_API_BASE_URL}/auth/stocks/sse/orders?clientId=${clientId}`
  esInstance = new EventSource(url, { withCredentials: true })

  esInstance.addEventListener('my-order', (e) => {
    try {
      const order: MyOrderSseDto = JSON.parse(e.data)
      listeners.forEach((fn) => fn(order))
    } catch {}
  })

  esInstance.onerror = () => {
    esInstance?.close()
    esInstance = null
    setTimeout(() => subscribeSSE(), 3000)
  }
}

function unsubscribeSSE() {
  esInstance?.close()
  esInstance = null
  listeners = []
}

// ─── Hook ───────────────────────────────────────────────────────
export function useOrderSSE() {
  const { isLoggedIn } = useAuthStore()
  const [latestOrder, setLatestOrder] = useState<MyOrderSseDto | null>(null)
  const [notifications, setNotifications] = useState<MyOrderSseDto[]>([])

  useEffect(() => {
    if (!isLoggedIn) return

    subscribeSSE()

    const handler = (order: MyOrderSseDto) => {
      setLatestOrder(order)
      setNotifications((prev) => [order, ...prev].slice(0, 20))
    }
    listeners.push(handler)

    return () => {
      listeners = listeners.filter((fn) => fn !== handler)
    }
  }, [isLoggedIn])

  useEffect(() => {
    if (!isLoggedIn) unsubscribeSSE()
  }, [isLoggedIn])

  const clearLatest = () => setLatestOrder(null)

  return { latestOrder, notifications, clearLatest }
}
