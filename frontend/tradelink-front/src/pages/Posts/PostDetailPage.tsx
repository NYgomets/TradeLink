import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { postsApi } from '../../api/posts'
import { useAuthStore } from '../../store/authStore'
import LikeButton from '../../components/posts/LikeButton'
import CommentSection from '../../components/posts/CommentSection'
import type { PostResponseDto, LikeStatusResponseDto } from '../../types'

export default function PostDetailPage() {
  const { postSeq } = useParams<{ postSeq: string }>()
  const navigate = useNavigate()
  const { member } = useAuthStore()
  const seq = Number(postSeq)

  const [post, setPost] = useState<PostResponseDto | null>(null)
  const [like, setLike] = useState<LikeStatusResponseDto | null>(null)
  const [loading, setLoading] = useState(true)
  const [selectedImage, setSelectedImage] = useState<string | null>(null)

  useEffect(() => {
    if (!seq) return
    setLoading(true)
    Promise.all([postsApi.getDetail(seq), postsApi.getLikeStatus(seq)])
      .then(([p, l]) => {
        setPost(p)
        setLike(l)
      })
      .catch(() => navigate('/posts'))
      .finally(() => setLoading(false))
  }, [seq])

  const handleDelete = async () => {
    if (!confirm('게시글을 삭제할까요?')) return
    try {
      await postsApi.delete(seq)
      navigate('/posts')
    } catch {
      alert('삭제에 실패했어요')
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-24">
        <div className="w-6 h-6 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
      </div>
    )
  }

  if (!post) return null
  const isMine = member?.memberName === post.authorName

  return (
    <div className="animate-fade-up max-w-3xl mx-auto">
      {/* 뒤로가기 */}
      <button
        onClick={() => navigate('/posts')}
        className="flex items-center gap-1.5 text-sm text-text-muted hover:text-text-secondary mb-6 transition-colors"
      >
        ← 목록으로
      </button>

      {/* 게시글 본문 */}
      <article className="bg-bg-card border border-border rounded-2xl p-6 mb-4">
        {/* 제목 */}
        <h1 className="text-xl font-semibold text-text-primary mb-3 leading-snug">
          {post.title}
        </h1>

        {/* 메타 */}
        <div className="flex items-center justify-between mb-5 pb-4 border-b border-border-subtle">
          <div className="flex items-center gap-3">
            <div className="w-7 h-7 rounded-full bg-bg-secondary border border-border flex items-center justify-center">
              <span className="font-mono text-xs text-text-muted">
                {post.authorName?.charAt(0)}
              </span>
            </div>
            <div>
              <span className="text-sm text-text-primary">{post.authorName}</span>
              <span className="text-xs text-text-muted ml-2">{post.createdAt?.slice(0, 16).replace('T', ' ')}</span>
            </div>
          </div>

          {isMine && (
            <div className="flex gap-1">
              <button
                onClick={() => navigate(`/posts/${seq}/edit`)}
                className="text-xs px-3 py-1.5 rounded-lg border border-border text-text-muted hover:text-text-secondary hover:border-accent-cyan/40 transition-all"
              >
                수정
              </button>
              <button
                onClick={handleDelete}
                className="text-xs px-3 py-1.5 rounded-lg border border-border text-text-muted hover:text-accent-red hover:border-accent-red/40 transition-all"
              >
                삭제
              </button>
            </div>
          )}
        </div>

        {/* 본문 */}
        <div className="text-sm text-text-primary leading-7 whitespace-pre-wrap mb-5">
          {post.content}
        </div>

        {/* 이미지 */}
        {post.imageUrls?.length > 0 && (
          <div className="flex flex-wrap gap-2 mb-5">
            {post.imageUrls.map((url, i) => (
              <img
                key={i}
                src={url}
                alt={`이미지 ${i + 1}`}
                onClick={() => setSelectedImage(url)}
                className="w-24 h-24 object-cover rounded-lg border border-border cursor-pointer hover:opacity-80 transition-opacity"
              />
            ))}
          </div>
        )}

        {/* 좋아요 */}
        <div className="flex items-center justify-end pt-4 border-t border-border-subtle">
          {like && (
            <LikeButton
              postSeq={seq}
              initialIsLiked={like.isLiked}
              initialCount={like.likeCount}
            />
          )}
        </div>
      </article>

      {/* 댓글 */}
      <div className="bg-bg-card border border-border rounded-2xl p-6">
        <CommentSection postSeq={seq} />
      </div>

      {/* 이미지 라이트박스 */}
      {selectedImage && (
        <div
          className="fixed inset-0 z-50 bg-black/80 backdrop-blur-sm flex items-center justify-center p-4"
          onClick={() => setSelectedImage(null)}
        >
          <img
            src={selectedImage}
            alt="확대 이미지"
            className="max-w-full max-h-[90vh] object-contain rounded-xl shadow-2xl"
          />
        </div>
      )}
    </div>
  )
}
