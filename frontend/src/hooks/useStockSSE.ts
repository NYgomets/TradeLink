import { useEffect, useRef, useState } from 'react'
import type { StockPriceSummaryDto, OrderBookDto } from '../types'

interface UseStockSSEResult {
  price: StockPriceSummaryDto | null
  orderBook: OrderBookDto | null
  connected: boolean
}

export function useStockSSE(ticker: string | null): UseStockSSEResult {
  const [price, setPrice] = useState<StockPriceSummaryDto | null>(null)
  const [orderBook, setOrderBook] = useState<OrderBookDto | null>(null)
  const [connected, setConnected] = useState(false)
  const esRef = useRef<EventSource | null>(null)

  useEffect(() => {
    if (!ticker) return

    const clientId = `client-${Date.now()}`
    const url = `/api/stocks/sse/${ticker}?clientId=${clientId}`
    const es = new EventSource(url, { withCredentials: true })
    esRef.current = es

    es.onopen = () => setConnected(true)

    es.addEventListener('stock-price', (e) => {
      try {
        setPrice(JSON.parse(e.data))
      } catch {}
    })

    es.addEventListener('order-book', (e) => {
      try {
        setOrderBook(JSON.parse(e.data))
      } catch {}
    })

    es.onerror = () => {
      setConnected(false)
      // 자동 재연결은 브라우저가 처리
    }

    return () => {
      es.close()
      setConnected(false)
    }
  }, [ticker])

  return { price, orderBook, connected }
}
