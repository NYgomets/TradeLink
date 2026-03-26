import { useEffect, useState } from 'react'
import { postsApi } from '../../api/posts'
import { useAuthStore } from '../../store/authStore'
import CommentItem from './CommentItem'
import type { CommentResponseDto } from '../../types'

interface Props {
  postSeq: number
}

export default function CommentSection({ postSeq }: Props) {
  const { isLoggedIn } = useAuthStore()
  const [comments, setComments] = useState<CommentResponseDto[]>([])
  const [newComment, setNewComment] = useState('')
  const [loading, setLoading] = useState(false)
  const [submitting, setSubmitting] = useState(false)

  const fetchComments = async () => {
    setLoading(true)
    try {
      const data = await postsApi.getComments(postSeq)
      setComments(data)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchComments()
  }, [postSeq])

  const totalCount = comments.reduce((acc, c) => acc + 1 + c.replies.length, 0)

  const handleSubmit = async () => {
    if (!newComment.trim()) return
    setSubmitting(true)
    try {
      await postsApi.createComment(postSeq, newComment.trim())
      setNewComment('')
      fetchComments()
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div>
      {/* 헤더 */}
      <div className="flex items-center gap-2 mb-4">
        <h3 className="text-sm font-semibold text-text-primary">댓글</h3>
        <span className="font-mono text-xs text-accent-cyan bg-accent-cyan/10 px-2 py-0.5 rounded-full">
          {totalCount}
        </span>
      </div>

      {/* 댓글 입력 */}
      {isLoggedIn ? (
        <div className="flex gap-2 mb-5">
          <input
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
            placeholder="댓글을 입력하세요..."
            className="flex-1 bg-bg-secondary border border-border rounded-xl px-4 py-2.5 text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent-cyan/40 transition-colors"
            onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && handleSubmit()}
          />
          <button
            onClick={handleSubmit}
            disabled={submitting || !newComment.trim()}
            className="px-4 py-2.5 bg-accent-cyan text-bg-primary rounded-xl text-sm font-medium hover:brightness-110 transition-all disabled:opacity-40 whitespace-nowrap"
          >
            {submitting ? '...' : '등록'}
          </button>
        </div>
      ) : (
        <div className="mb-5 py-3 px-4 bg-bg-secondary rounded-xl border border-border text-sm text-text-muted text-center">
          <a href="/login" className="text-accent-cyan hover:underline">로그인</a>
          하면 댓글을 달 수 있어요
        </div>
      )}

      {/* 댓글 목록 */}
      {loading ? (
        <div className="flex justify-center py-8">
          <div className="w-5 h-5 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
        </div>
      ) : comments.length === 0 ? (
        <div className="text-center py-8 text-text-muted text-sm">
          첫 댓글을 남겨보세요
        </div>
      ) : (
        <div className="divide-y divide-border-subtle">
          {comments.map((comment) => (
            <CommentItem
              key={comment.commentSeq}
              comment={comment}
              postSeq={postSeq}
              onRefresh={fetchComments}
            />
          ))}
        </div>
      )}
    </div>
  )
}
