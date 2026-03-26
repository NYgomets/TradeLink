import { useRef, useState } from 'react'
import { postsApi } from '../../api/posts'

interface UploadedImage {
  s3Key: string
  previewUrl: string
  name: string
}

interface Props {
  images: UploadedImage[]
  onChange: (images: UploadedImage[]) => void
}

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_FILES = 5

export default function ImageUploader({ images, onChange }: Props) {
  const inputRef = useRef<HTMLInputElement>(null)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')

  const handleFiles = async (files: FileList | null) => {
    if (!files || files.length === 0) return
    setError('')

    const toUpload = Array.from(files).slice(0, MAX_FILES - images.length)
    if (toUpload.length === 0) {
      setError(`이미지는 최대 ${MAX_FILES}개까지 업로드할 수 있어요`)
      return
    }

    const invalid = toUpload.find((f) => !ALLOWED_TYPES.includes(f.type))
    if (invalid) {
      setError('JPG, PNG, WEBP 형식만 업로드할 수 있어요')
      return
    }

    setUploading(true)
    try {
      const results = await Promise.all(
        toUpload.map(async (file) => {
          const { preSignedUrl, s3Key } = await postsApi.getPresignedUrl(file.name, file.type)
          await postsApi.uploadToS3(preSignedUrl, file)
          return {
            s3Key,
            previewUrl: URL.createObjectURL(file),
            name: file.name,
          }
        })
      )
      onChange([...images, ...results])
    } catch {
      setError('업로드 중 오류가 발생했어요')
    } finally {
      setUploading(false)
    }
  }

  const removeImage = (s3Key: string) => {
    onChange(images.filter((img) => img.s3Key !== s3Key))
  }

  return (
    <div>
      <div className="flex flex-wrap gap-2 mb-2">
        {/* 기존 이미지 */}
        {images.map((img) => (
          <div key={img.s3Key} className="relative group w-20 h-20">
            <img
              src={img.previewUrl}
              alt={img.name}
              className="w-full h-full object-cover rounded-lg border border-border"
            />
            <button
              onClick={() => removeImage(img.s3Key)}
              className="absolute -top-1.5 -right-1.5 w-5 h-5 bg-accent-red text-white rounded-full text-[10px] flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity hover:brightness-110"
            >
              ✕
            </button>
          </div>
        ))}

        {/* 업로드 버튼 */}
        {images.length < MAX_FILES && (
          <button
            onClick={() => inputRef.current?.click()}
            disabled={uploading}
            className="w-20 h-20 rounded-lg border border-dashed border-border hover:border-accent-cyan/40 text-text-muted hover:text-accent-cyan transition-all flex flex-col items-center justify-center gap-1 disabled:opacity-40"
          >
            {uploading ? (
              <div className="w-4 h-4 border-2 border-accent-cyan/30 border-t-accent-cyan rounded-full animate-spin" />
            ) : (
              <>
                <span className="text-xl leading-none">+</span>
                <span className="text-[10px]">사진 추가</span>
              </>
            )}
          </button>
        )}
      </div>

      <input
        ref={inputRef}
        type="file"
        accept={ALLOWED_TYPES.join(',')}
        multiple
        className="hidden"
        onChange={(e) => handleFiles(e.target.files)}
      />

      {error && <p className="text-xs text-accent-red mt-1">{error}</p>}
      <p className="text-xs text-text-muted mt-1">
        JPG, PNG, WEBP · 최대 {MAX_FILES}개
      </p>
    </div>
  )
}
