import { Link, useLocation } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'

const NAV = [
  { label: '환율', path: '/exchange-rates' },
  { label: '모의투자', path: '/stocks' },
  { label: '포트폴리오', path: '/portfolio' },
  { label: '커뮤니티', path: '/posts' },
]

export default function Header() {
  const { pathname } = useLocation()
  const { member, isLoggedIn, logout } = useAuthStore()

  return (
    <header className="fixed top-0 left-0 right-0 z-50 border-b border-border bg-bg-primary/90 backdrop-blur-md">
      <div className="mx-auto flex max-w-7xl items-center justify-between px-6 h-14">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2 group">
          <span className="font-mono text-lg font-semibold tracking-tight text-text-primary">
            Trade<span className="text-accent-cyan">Link</span>
          </span>
        </Link>

        {/* Nav */}
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
            <div className="flex gap-2">
              <a
                href="http://localhost:17771/oauth2/authorization/google"
                className="flex items-center gap-1.5 text-xs px-3 py-1.5 rounded border border-border text-text-secondary hover:text-text-primary hover:border-accent-cyan/40 transition-all"
              >
                Google
              </a>
              <a
                href="http://localhost:17771/oauth2/authorization/kakao"
                className="flex items-center gap-1.5 text-xs px-3 py-1.5 rounded bg-[#FEE500] text-[#3C1E1E] font-medium hover:bg-[#f0d800] transition-all"
              >
                Kakao
              </a>
              <a
                href="http://localhost:17771/oauth2/authorization/naver"
                className="flex items-center gap-1.5 text-xs px-3 py-1.5 rounded bg-[#03C75A] text-white font-medium hover:bg-[#02b350] transition-all"
              >
                Naver
              </a>
            </div>
          )}
        </div>
      </div>
    </header>
  )
}
