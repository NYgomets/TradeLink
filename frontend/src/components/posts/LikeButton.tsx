import { useState } from 'react'
import { postsApi } from '../../api/posts'
import { useAuthStore } from '../../store/authStore'
import { useNavigate } from 'react-router-dom'

interface Props {
  postSeq: number
  initialIsLiked: boolean
  initialCount: number
}

export default function LikeButton({ postSeq, initialIsLiked, initialCount }: Props) {
  const { isLoggedIn } = useAuthStore()
  const navigate = useNavigate()
  const [isLiked, setIsLiked] = useState(initialIsLiked)
  const [count, setCount] = useState(initialCount)
  const [loading, setLoading] = useState(false)

  const handleToggle = async () => {
    if (!isLoggedIn) {
      navigate('/login')
      return
    }
    if (loading) return
    setLoading(true)

    // 낙관적 업데이트
    const next = !isLiked
    setIsLiked(next)
    setCount((c) => (next ? c + 1 : Math.max(0, c - 1)))

    try {
      const res = await postsApi.toggleLike(postSeq, next ? 'LIKE' : 'UNLIKE')
      setIsLiked(res.isLiked)
      setCount(res.likeCount)
    } catch {
      // 실패 시 롤백
      setIsLiked(!next)
      setCount((c) => (next ? Math.max(0, c - 1) : c + 1))
    } finally {
      setLoading(false)
    }
  }

  return (
    <button
      onClick={handleToggle}
      disabled={loading}
      className={`flex items-center gap-2 px-4 py-2 rounded-xl border text-sm font-medium transition-all ${
        isLiked
          ? 'bg-accent-red/10 border-accent-red/40 text-accent-red'
          : 'border-border text-text-secondary hover:border-accent-red/40 hover:text-accent-red hover:bg-accent-red/5'
      }`}
    >
      <span className={`text-base transition-transform ${isLiked ? 'scale-125' : ''}`}>
        {isLiked ? '❤️' : '🤍'}
      </span>
      <span className="font-mono">{count.toLocaleString()}</span>
    </button>
  )
}
