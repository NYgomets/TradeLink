import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'

const NAV = [
  { label: '환율', path: '/exchange-rates' },
  { label: '모의투자', path: '/stocks' },
  { label: '포트폴리오', path: '/portfolio' },
  { label: '커뮤니티', path: '/posts' },
]

const BACKEND_URL = import.meta.env.VITE_BACKEND_URL

export default function Header() {
  const { pathname } = useLocation()
  const { member, isLoggedIn, logout } = useAuthStore()
  const [menuOpen, setMenuOpen] = useState(false)

  return (
    <header className="fixed top-0 left-0 right-0 z-50 border-b border-border bg-bg-primary/90 backdrop-blur-md">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-6 h-14">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2 group">
          <span className="font-mono text-lg font-semibold tracking-tight text-text-primary">
            Trade<span className="text-accent-cyan">Link</span>
          </span>
        </Link>

        {/* Nav - PC */}
        <nav className="hidden md:flex items-center gap-1">
          {NAV.map((item) => {
            const active = pathname.startsWith(item.path)
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`px-4 py-1.5 rounded text-sm font-medium transition-all duration-150 ${
                  active
                    ? 'text-accent-cyan bg-accent-cyan/10'
                    : 'text-text-secondary hover:text-text-primary hover:bg-bg-hover'
                }`}
              >
                {item.label}
              </Link>
            )
          })}
        </nav>

        {/* Right */}
        <div className="flex items-center gap-3">
          {isLoggedIn && member ? (
            <>
              <div className="hidden sm:flex flex-col items-end">
                <span className="text-xs text-text-secondary">{member.memberName}</span>
                <span className="font-mono text-xs text-accent-cyan">
                  {member.availableBalance.toLocaleString()}원
                </span>
              </div>
              <button
                onClick={logout}
                className="text-xs text-text-muted hover:text-text-secondary transition-colors px-3 py-1.5 rounded border border-border hover:border-border-subtle"
              >
                로그아웃
              </button>
            </>
          ) : (
            <div className="hidden md:flex gap-2">
              <a href={`${BACKEND_URL}/oauth2/authorization/google`}
                className="flex items-center gap-1.5 text-xs px-3 py-1.5 rounded border border-border text-text-secondary hover:text-text-primary hover:border-accent-cyan/40 transition-all">
                Google
              </a>
              <a href={`${BACKEND_URL}/oauth2/authorization/kakao`}
                className="flex items-center gap-1.5 text-xs px-3 py-1.5 rounded bg-[#FEE500] text-[#3C1E1E] font-medium hover:bg-[#f0d800] transition-all">
                Kakao
              </a>
              <a href={`${BACKEND_URL}/oauth2/authorization/naver`}
                className="flex items-center gap-1.5 text-xs px-3 py-1.5 rounded bg-[#03C75A] text-white font-medium hover:bg-[#02b350] transition-all">
                Naver
              </a>
            </div>
          )}

          {/* 햄버거 버튼 - 모바일 */}
          <button
            onClick={() => setMenuOpen(v => !v)}
            className="md:hidden flex flex-col gap-1.5 p-2"
          >
            <span className={`block w-5 h-0.5 bg-text-primary transition-all ${menuOpen ? 'rotate-45 translate-y-2' : ''}`} />
            <span className={`block w-5 h-0.5 bg-text-primary transition-all ${menuOpen ? 'opacity-0' : ''}`} />
            <span className={`block w-5 h-0.5 bg-text-primary transition-all ${menuOpen ? '-rotate-45 -translate-y-2' : ''}`} />
          </button>
        </div>
      </div>

      {/* 모바일 메뉴 */}
      {menuOpen && (
        <div className="md:hidden border-t border-border bg-bg-primary/95 backdrop-blur-md px-6 py-4 flex flex-col gap-2">
          {NAV.map((item) => {
            const active = pathname.startsWith(item.path)
            return (
              <Link
                key={item.path}
                to={item.path}
                onClick={() => setMenuOpen(false)}
                className={`px-4 py-2.5 rounded text-sm font-medium transition-all ${
                  active
                    ? 'text-accent-cyan bg-accent-cyan/10'
                    : 'text-text-secondary hover:text-text-primary hover:bg-bg-hover'
                }`}
              >
                {item.label}
              </Link>
            )
          })}
          {!isLoggedIn && (
            <div className="flex flex-col gap-2 mt-2 pt-2 border-t border-border">
              <a href={`${BACKEND_URL}/oauth2/authorization/google`}
                className="flex items-center justify-center gap-2 py-2.5 rounded border border-border text-sm text-text-secondary hover:text-text-primary transition-all">
                Google로 로그인
              </a>
              <a href={`${BACKEND_URL}/oauth2/authorization/kakao`}
                className="flex items-center justify-center gap-2 py-2.5 rounded bg-[#FEE500] text-[#3C1E1E] text-sm font-medium transition-all">
                카카오로 로그인
              </a>
              <a href={`${BACKEND_URL}/oauth2/authorization/naver`}
                className="flex items-center justify-center gap-2 py-2.5 rounded bg-[#03C75A] text-white text-sm font-medium transition-all">
                네이버로 로그인
              </a>
            </div>
          )}
          {isLoggedIn && member && (
            <div className="flex items-center justify-between mt-2 pt-2 border-t border-border">
              <span className="text-xs text-text-secondary">{member.memberName}</span>
              <button onClick={logout} className="text-xs text-text-muted hover:text-text-secondary transition-colors">
                로그아웃
              </button>
            </div>
          )}
        </div>
      )}
    </header>
  )
}