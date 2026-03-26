import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { postsApi } from '../../api/posts'
import { useAuthStore } from '../../store/authStore'
import type { PostSummaryDto, PageResponse } from '../../types'

export default function PostsPage() {
  const navigate = useNavigate()
  const { isLoggedIn } = useAuthStore()
  const [result, setResult] = useState<PageResponse<PostSummaryDto> | null>(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setLoading(true)
    postsApi
      .getList(page, 10)
      .then(setResult)
      .catch(() => {})
      .finally(() => setLoading(false))
  }, [page])

  return (
    <div className="animate-fade-up">
      {/* 헤더 */}
      <div className="flex items-end justify-between mb-8">
        <div>
          <p className="text-xs font-mono uppercase tracking-widest text-accent-cyan mb-1">
            Community
          </p>
          <h1 className="text-2xl font-semibold text-text-primary">커뮤니티</h1>
        </div>
        {isLoggedIn && (
          <button
            onClick={() => navigate('/posts/write')}
            className="flex items-center gap-2 px-4 py-2 bg-accent-cyan text-bg-primary rounded-xl text-sm font-semibold hover:brightness-110 transition-all"
          >
            <span>+</span> 글 작성
          </button>
        )}
      </div>

      {/* 게시글 목록 */}
      <div className="bg-bg-card border border-border rounded-2xl overflow-hidden">
        {/* 테이블 헤더 */}
        <div className="hidden sm:grid grid-cols-12 px-5 py-2.5 border-b border-border text-xs text-text-muted font-mono uppercase tracking-wider">
          <span className="col-span-7">제목</span>
          <span className="col-span-2 text-center">작성자</span>
          <span className="col-span-2 text-right">작성일</span>
          <span className="col-span-1 text-right">댓글</span>
        </div>

        {loading ? (
          <div className="flex justify-center py-16">
            <div className="w-6 h-6 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
          </div>
        ) : !result?.content.length ? (
          <div className="py-16 text-center text-text-muted text-sm">
            아직 게시글이 없어요
          </div>
        ) : (
          <ul className="divide-y divide-border-subtle">
            {result.content.map((post, i) => (
              <li
                key={post.seq}
                onClick={() => navigate(`/posts/${post.seq}`)}
                className="grid grid-cols-12 items-center px-5 py-3.5 hover:bg-bg-hover cursor-pointer transition-colors animate-fade-up"
                style={{ animationDelay: `${i * 25}ms` }}
              >
                {/* 제목 */}
                <div className="col-span-12 sm:col-span-7 flex flex-col gap-1 min-w-0">
                  {/* 제목 */}
                  <div className="flex items-center gap-2">
                    <span className="text-sm text-text-primary truncate hover:text-accent-cyan transition-colors">
                      {post.title}
                    </span>

                    {post.hasFiles && (
                      <span className="text-[10px] bg-accent-cyan/10 text-accent-cyan px-1.5 py-0.5 rounded font-mono">
                        IMG
                      </span>
                    )}
                  </div>

                  {/* 👇 이 줄 추가 (모바일용) */}
                  <div className="flex items-center gap-3 text-xs text-text-muted sm:hidden">
                    <span>{post.authorName}</span>
                    <span>·</span>
                    <span>{post.createTime?.slice(0, 10)}</span>
                    <span>·</span>
                    <span className="text-accent-cyan">
                      댓글 {post.commentCount}
                    </span>
                  </div>
                </div>

                {/* 작성자 */}
                <div className="hidden sm:flex col-span-2 justify-center">
                  <span className="text-xs text-text-secondary truncate">{post.authorName}</span>
                </div>

                {/* 날짜 */}
                <div className="hidden sm:flex col-span-2 justify-end">
                  <span className="font-mono text-xs text-text-muted">
                    {post.createTime?.slice(0, 10)}
                  </span>
                </div>

                {/* 댓글 수 */}
                <div className="hidden sm:flex col-span-1 justify-end">
                  <span className="font-mono text-xs text-accent-cyan">
                      [{post.commentCount}]
                  </span>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>

      {/* 페이지네이션 */}
      {result && result.totalPages > 1 && (
        <div className="flex items-center justify-center gap-1 mt-6">
          <button
            onClick={() => setPage((p) => Math.max(0, p - 1))}
            disabled={result.first}
            className="w-8 h-8 rounded border border-border text-text-secondary hover:text-text-primary hover:border-accent-cyan/40 disabled:opacity-30 disabled:cursor-not-allowed transition-all text-sm"
          >
            ‹
          </button>

          {Array.from({ length: result.totalPages }, (_, i) => i)
            .filter((i) => Math.abs(i - page) <= 2)
            .map((i) => (
              <button
                key={i}
                onClick={() => setPage(i)}
                className={`w-8 h-8 rounded border font-mono text-sm transition-all ${
                  i === page
                    ? 'bg-accent-cyan text-bg-primary border-accent-cyan'
                    : 'border-border text-text-secondary hover:text-text-primary hover:border-accent-cyan/40'
                }`}
              >
                {i + 1}
              </button>
            ))}

          <button
            onClick={() => setPage((p) => Math.min(result.totalPages - 1, p + 1))}
            disabled={result.last}
            className="w-8 h-8 rounded border border-border text-text-secondary hover:text-text-primary hover:border-accent-cyan/40 disabled:opacity-30 disabled:cursor-not-allowed transition-all text-sm"
          >
            ›
          </button>
        </div>
      )}
    </div>
  )
}
