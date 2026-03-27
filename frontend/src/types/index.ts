// ─── Auth / Member ───────────────────────────────────────────────
export interface MemberDto {
  memberSeq: number
  memberName: string
  email: string
  provider: string
  balance: number
  availableBalance: number
}

// ─── Exchange Rate ────────────────────────────────────────────────
export interface ExchangeRateSummaryDto {
  currencyCode: string
  currencyName: string
  rate: number
  changeAmount: number
  changePercent: number
  baseDateTime: string
}

export interface ExchangeRateChartPointDto {
  rate: number
  baseDateTime: string
}

// ─── Post ─────────────────────────────────────────────────────────
export interface PostSummaryDto {
  seq: number
  title: string
  authorName: string
  createTime: string
  commentCount: number
  hasFiles: boolean
}

export interface PostResponseDto {
  postSeq: number
  title: string
  content: string
  authorName: string
  imageUrls: string[]
  createdAt: string
}

export interface PageResponse<T> {
  content: T[]
  totalPages: number
  totalElements: number
  number: number
  size: number
  first: boolean
  last: boolean
}

// ─── Comment ──────────────────────────────────────────────────────
export interface CommentResponseDto {
  commentSeq: number
  memberSeq: number
  content: string
  authorName: string
  createTime: string
  depth: number
  replies: CommentResponseDto[]
}

// ─── Like ─────────────────────────────────────────────────────────
export interface LikeStatusResponseDto {
  postSeq: number
  isLiked: boolean
  likeCount: number
}

export interface LikePostResponseDto {
  isLiked: boolean
  likeCount: number
}

// ─── Stock / Crypto ───────────────────────────────────────────────
export interface StockPriceSummaryDto {
  ticker: string
  name: string
  price: number
  changePercent: number
  changeAmount: number
  volume: number
}

export interface OrderBookDto {
  ticker: string
  asks: OrderBookLevel[]
  bids: OrderBookLevel[]
}

export interface OrderBookLevel {
  price: number
  quantity: number
}

export interface HoldingDto {
  ticker: string
  name: string
  quantity: number
  avgPrice: number
}

export interface WalletDto {
  balance: number
  availableBalance: number
}

export interface TotalAssetDto {
  balance: number
  availableBalance: number
  holdingValue: number
  totalAsset: number
}

export interface TradeHistoryDto {
  seq: number
  ticker: string
  name: string
  side: 'BUY' | 'SELL'
  quantity: number
  execPrice: number
  totalAmount: number
  tradedAt: string
}

export interface CursorPageResponse<T> {
  content: T[]
  nextCursor: number | null
  hasNext: boolean
}

// ─── API Common ───────────────────────────────────────────────────
export interface ApiResponse<T> {
  data: T
  message?: string
  status?: number
}

// ✅ 1D 추가
export type Period = '1D' | '1W' | '1M' | '3M' | '6M' | '1Y'
