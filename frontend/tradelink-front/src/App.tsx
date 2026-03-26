import { useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from './store/authStore'
import Layout from './components/layout/Layout'
import ProtectedRoute from './components/common/ProtectedRoute'
import ExchangeRatePage from './pages/ExchangeRate/ExchangeRatePage'
import StocksPage from './pages/Stocks/StocksPage'
import StockDetailPage from './pages/Stocks/StockDetailPage'
import PortfolioPage from './pages/Portfolio/PortfolioPage'
import PostsPage from './pages/Posts/PostsPage'
import PostDetailPage from './pages/Posts/PostDetailPage'
import PostWritePage from './pages/Posts/PostWritePage'
import LoginPage from './pages/Login/LoginPage'

export default function App() {
  const { fetchMe } = useAuthStore()

  useEffect(() => {
    fetchMe()
  }, [fetchMe])

  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Navigate to="/exchange-rates" replace />} />
          <Route path="/exchange-rates" element={<ExchangeRatePage />} />
          <Route path="/stocks" element={<StocksPage />} />
          <Route path="/stocks/:ticker" element={<StockDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/posts" element={<PostsPage />} />
          <Route path="/posts/:postSeq" element={<PostDetailPage />} />

          {/* 로그인 필요한 페이지 */}
          <Route path="/portfolio" element={
            <ProtectedRoute><PortfolioPage /></ProtectedRoute>
          } />
          <Route path="/posts/write" element={
            <ProtectedRoute><PostWritePage /></ProtectedRoute>
          } />
          <Route path="/posts/:postSeq/edit" element={
            <ProtectedRoute><PostWritePage /></ProtectedRoute>
          } />

          {/* 없는 경로는 메인으로 */}
          <Route path="*" element={<Navigate to="/exchange-rates" replace />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
