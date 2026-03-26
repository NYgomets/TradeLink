import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { postsApi } from '../../api/posts'
import { useAuthStore } from '../../store/authStore'
import ImageUploader from '../../components/posts/ImageUploader'
import client from '../../api/client'

interface UploadedImage {
  s3Key: string
  previewUrl: string
  name: string
}

export default function PostWritePage() {
  const navigate = useNavigate()
  const { postSeq } = useParams<{ postSeq: string }>()
  const { isLoggedIn } = useAuthStore()
  const isEdit = !!postSeq
  const seq = Number(postSeq)

  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')
  const [images, setImages] = useState<UploadedImage[]>([])
  const [loading, setLoading] = useState(false)
  const [initLoading, setInitLoading] = useState(isEdit)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEdit) return
    postsApi
      .getDetail(seq)
      .then((post) => {
        setTitle(post.title)
        setContent(post.content)
        setImages(
          post.imageUrls.map((url, i) => ({
            s3Key: url,
            previewUrl: url,
            name: `image-${i}`,
          }))
        )
      })
      .catch(() => navigate('/posts'))
      .finally(() => setInitLoading(false))
  }, [isEdit, seq])

  useEffect(() => {
    if (!isLoggedIn) navigate('/login')
  }, [isLoggedIn])

  const handleSubmit = async () => {
    if (!title.trim()) { setError('제목을 입력해주세요'); return }
    if (!content.trim()) { setError('내용을 입력해주세요'); return }

    setLoading(true)
    setError('')
    try {
      const s3Keys = images.map((img) => img.s3Key)
      if (isEdit) {
        await postsApi.update(seq, title.trim(), content.trim(), s3Keys)
        navigate(`/posts/${seq}`)
      } else {
        const newSeq = await postsApi.create(title.trim(), content.trim(), s3Keys)
        navigate(`/posts/${newSeq}`)
      }
    } catch (e: any) {
      setError(e.response?.data?.message ?? '저장 중 오류가 발생했어요')
    } finally {
      setLoading(false)
    }
  }

  if (initLoading) {
    return (
      <div className="flex justify-center py-24">
        <div className="w-6 h-6 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
      </div>
    )
  }

  return (
    <div className="animate-fade-up max-w-3xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <p className="text-xs font-mono uppercase tracking-widest text-accent-cyan mb-1">
            Community
          </p>
          <h1 className="text-2xl font-semibold text-text-primary">
            {isEdit ? '게시글 수정' : '새 글 작성'}
          </h1>
        </div>
        <button
          onClick={() => navigate(-1)}
          className="text-sm text-text-muted hover:text-text-secondary transition-colors"
        >
          취소
        </button>
      </div>

      <div className="bg-bg-card border border-border rounded-2xl p-6 flex flex-col gap-5">
        <div>
          <label className="text-xs text-text-muted mb-1.5 block">제목</label>
          <input
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="제목을 입력하세요"
            maxLength={100}
            className="w-full bg-bg-secondary border border-border rounded-xl px-4 py-3 text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent-cyan/40 transition-colors"
          />
          <div className="text-right text-xs text-text-muted mt-1">{title.length}/100</div>
        </div>

        <div>
          <label className="text-xs text-text-muted mb-1.5 block">내용</label>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="내용을 입력하세요..."
            rows={12}
            className="w-full bg-bg-secondary border border-border rounded-xl px-4 py-3 text-text-primary placeholder:text-text-muted focus:outline-none focus:border-accent-cyan/40 transition-colors resize-none leading-relaxed"
          />
        </div>

        <div>
          <label className="text-xs text-text-muted mb-1.5 block">이미지</label>
          <ImageUploader images={images} onChange={setImages} />
        </div>

        {error && (
          <div className="text-xs text-accent-red bg-accent-red/10 border border-accent-red/20 px-4 py-2.5 rounded-xl">
            {error}
          </div>
        )}

        <div className="flex gap-2 pt-1">
          <button
            onClick={() => navigate(-1)}
            className="flex-1 py-3 rounded-xl border border-border text-text-secondary hover:text-text-primary hover:border-accent-cyan/30 transition-all text-sm"
          >
            취소
          </button>
          <button
            onClick={handleSubmit}
            disabled={loading}
            className="flex-1 py-3 bg-accent-cyan text-bg-primary rounded-xl text-sm font-semibold hover:brightness-110 transition-all disabled:opacity-40 disabled:cursor-not-allowed"
          >
            {loading ? (
              <span className="flex items-center justify-center gap-2">
                <span className="w-4 h-4 border-2 border-bg-primary/30 border-t-bg-primary rounded-full animate-spin" />
                저장중...
              </span>
            ) : isEdit ? '수정 완료' : '등록하기'}
          </button>
        </div>
      </div>
    </div>
  )
}
