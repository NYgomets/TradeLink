import { useState } from 'react'
import { postsApi } from '../../api/posts'
import { useAuthStore } from '../../store/authStore'
import type { CommentResponseDto } from '../../types'

interface Props {
  comment: CommentResponseDto
  postSeq: number
  onRefresh: () => void
}

export default function CommentItem({ comment, postSeq, onRefresh }: Props) {
  const { member, isLoggedIn } = useAuthStore()
  const isMine = member?.memberSeq === comment.memberSeq
  const isDeleted = comment.content === '삭제된 댓글입니다.'

  const [showReplyInput, setShowReplyInput] = useState(false)
  const [replyContent, setReplyContent] = useState('')
  const [isEditing, setIsEditing] = useState(false)
  const [editContent, setEditContent] = useState(comment.content)
  const [loading, setLoading] = useState(false)

  const handleReply = async () => {
    if (!replyContent.trim()) return
    setLoading(true)
    try {
      await postsApi.createComment(postSeq, replyContent.trim(), comment.commentSeq)
      setReplyContent('')
      setShowReplyInput(false)
      onRefresh()
    } finally {
      setLoading(false)
    }
  }

  const handleEdit = async () => {
    if (!editContent.trim()) return
    setLoading(true)
    try {
      await postsApi.updateComment(postSeq, comment.commentSeq, editContent.trim())
      setIsEditing(false)
      onRefresh()
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async () => {
    if (!confirm('댓글을 삭제할까요?')) return
    setLoading(true)
    try {
      await postsApi.deleteComment(postSeq, comment.commentSeq)
      onRefresh()
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className={`${comment.depth > 0 ? 'ml-8 pl-4 border-l border-border-subtle' : ''}`}>
      <div className="py-3">
        {/* 작성자 + 시간 */}
        <div className="flex items-center justify-between mb-1.5">
          <div className="flex items-center gap-2">
            <div className="w-6 h-6 rounded-full bg-bg-secondary border border-border flex items-center justify-center">
              <span className="text-[10px] text-text-muted font-mono">
                {comment.authorName?.charAt(0) ?? '?'}
              </span>
            </div>
            <span className={`text-sm font-medium ${isDeleted ? 'text-text-muted' : 'text-text-primary'}`}>
              {isDeleted ? '알 수 없음' : comment.authorName}
            </span>
            {comment.depth > 0 && (
              <span className="text-[10px] text-accent-cyan/60 font-mono">↩ 답글</span>
            )}
            <span className="text-xs text-text-muted">{comment.createTime}</span>
          </div>

          {/* 수정/삭제 버튼 */}
          {isMine && !isDeleted && (
            <div className="flex gap-1">
              <button
                onClick={() => { setIsEditing(true); setEditContent(comment.content) }}
                className="text-xs text-text-muted hover:text-text-secondary px-1.5 py-0.5 rounded hover:bg-bg-hover transition-colors"
              >
                수정
              </button>
              <button
                onClick={handleDelete}
                disabled={loading}
                className="text-xs text-text-muted hover:text-accent-red px-1.5 py-0.5 rounded hover:bg-accent-red/5 transition-colors"
              >
                삭제
              </button>
            </div>
          )}
        </div>

        {/* 내용 */}
        {isEditing ? (
          <div className="flex gap-2 mt-1">
            <input
              value={editContent}
              onChange={(e) => setEditContent(e.target.value)}
              className="flex-1 bg-bg-secondary border border-border rounded-lg px-3 py-1.5 text-sm text-text-primary focus:outline-none focus:border-accent-cyan/40"
              onKeyDown={(e) => e.key === 'Enter' && handleEdit()}
            />
            <button
              onClick={handleEdit}
              disabled={loading}
              className="px-3 py-1.5 bg-accent-cyan text-bg-primary rounded-lg text-xs font-medium hover:brightness-110 transition-all"
            >
              저장
            </button>
            <button
              onClick={() => setIsEditing(false)}
              className="px-3 py-1.5 text-text-muted border border-border rounded-lg text-xs hover:text-text-secondary transition-colors"
            >
              취소
            </button>
          </div>
        ) : (
          <p className={`text-sm leading-relaxed ${isDeleted ? 'text-text-muted italic' : 'text-text-primary'}`}>
            {comment.content}
          </p>
        )}

        {/* 답글 버튼 (depth 0만) */}
        {!isDeleted && comment.depth === 0 && isLoggedIn && !isEditing && (
          <button
            onClick={() => setShowReplyInput((v) => !v)}
            className="mt-1.5 text-xs text-text-muted hover:text-accent-cyan transition-colors"
          >
            {showReplyInput ? '취소' : '↩ 답글'}
          </button>
        )}

        {/* 답글 입력창 */}
        {showReplyInput && (
          <div className="flex gap-2 mt-2">
            <input
              value={replyContent}
              onChange={(e) => setReplyContent(e.target.value)}
              placeholder="답글을 입력하세요..."
              className="flex-1 bg-bg-secondary border border-border rounded-lg px-3 py-1.5 text-sm text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent-cyan/40"
              onKeyDown={(e) => e.key === 'Enter' && handleReply()}
            />
            <button
              onClick={handleReply}
              disabled={loading || !replyContent.trim()}
              className="px-3 py-1.5 bg-accent-cyan text-bg-primary rounded-lg text-xs font-medium hover:brightness-110 transition-all disabled:opacity-40"
            >
              등록
            </button>
          </div>
        )}
      </div>

      {/* 대댓글 재귀 렌더링 */}
      {comment.replies?.map((reply) => (
        <CommentItem
          key={reply.commentSeq}
          comment={reply}
          postSeq={postSeq}
          onRefresh={onRefresh}
        />
      ))}
    </div>
  )
}
