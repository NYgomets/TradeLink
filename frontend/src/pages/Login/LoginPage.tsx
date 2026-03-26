const BACKEND_URL = import.meta.env.VITE_BACKEND_URL;

export default function LoginPage() {
  return (
    <div className="min-h-[80vh] flex items-center justify-center">
      <div className="w-full max-w-sm animate-fade-up">
        <div className="text-center mb-8">
          <p className="font-mono text-xs text-accent-cyan uppercase tracking-widest mb-2">
            TradeLink
          </p>
          <h1 className="text-2xl font-semibold text-text-primary">로그인</h1>
          <p className="text-sm text-text-secondary mt-2">소셜 계정으로 간편하게 시작하세요</p>
        </div>

        <div className="bg-bg-card border border-border rounded-2xl p-6 flex flex-col gap-3">
          {/* 구글 로그인 */}
          <a
            href={`${BACKEND_URL}/oauth2/authorization/google`}
            className="flex items-center justify-center gap-3 w-full py-3 px-4 rounded-xl border border-border hover:border-text-muted hover:bg-bg-hover transition-all text-sm text-text-primary"
          >
            <svg className="w-4 h-4" viewBox="0 0 24 24">
              <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
              <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
              <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
              <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
            </svg>
            Google로 계속하기
          </a>

          {/* 카카오 로그인 */}
          <a
            href={`${BACKEND_URL}/oauth2/authorization/kakao`}
            className="flex items-center justify-center gap-3 w-full py-3 px-4 rounded-xl bg-[#FEE500] hover:bg-[#f0d800] transition-all text-sm text-[#3C1E1E] font-medium"
          >
            <svg className="w-4 h-4" viewBox="0 0 24 24" fill="#3C1E1E">
              <path d="M12 3C6.48 3 2 6.69 2 11.25c0 2.91 1.87 5.47 4.71 6.97l-1.2 4.47 5.19-3.44c.42.06.85.09 1.3.09 5.52 0 10-3.69 10-8.25C22 6.69 17.52 3 12 3z"/>
            </svg>
            카카오로 계속하기
          </a>

          {/* 네이버 로그인 */}
          <a
            href={`${BACKEND_URL}/oauth2/authorization/naver`}
            className="flex items-center justify-center gap-3 w-full py-3 px-4 rounded-xl bg-[#03C75A] hover:bg-[#02b350] transition-all text-sm text-white font-medium"
          >
            <span className="font-bold text-base leading-none">N</span>
            네이버로 계속하기
          </a>
        </div>
      </div>
    </div>
  );
}