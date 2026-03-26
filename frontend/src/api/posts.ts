import client from './client'
import type {
  PostSummaryDto,
  PostResponseDto,
  CommentResponseDto,
  LikeStatusResponseDto,
  LikePostResponseDto,
  PageResponse,
} from '../types'

export const postsApi = {
  // ─── 게시글 ───────────────────────────────────────────
  getList: (page = 0, size = 10) =>
    client
      .get<{ data: PageResponse<PostSummaryDto> }>('/posts', { params: { page, size } })
      .then((r) => r.data.data),

  getDetail: (postSeq: number) =>
    client.get<{ data: PostResponseDto }>(`/posts/${postSeq}`).then((r) => r.data.data),

  // presigned URL 발급 → S3 직접 업로드
  getPresignedUrl: (fileName: string, contentType: string) =>
    client
      .get<{ data: { preSignedUrl: string; s3Key: string } }>('/auth/posts/presigned-url', {
        params: { fileName, contentType },
      })
      .then((r) => r.data.data),

  uploadToS3: (presignedUrl: string, file: File) =>
    fetch(presignedUrl, {
      method: 'PUT',
      headers: { 'Content-Type': file.type },
      body: file,
    }),

  create: (title: string, content: string, s3Keys: string[]) =>
    client
      .post<{ data: number }>('/auth/posts', { title, content, s3Keys })
      .then((r) => r.data.data),

  update: (postSeq: number, title: string, content: string, s3Keys: string[]) =>
    client.put(`/auth/posts/${postSeq}`, { title, content, s3Keys }),

  delete: (postSeq: number) =>
    client.delete(`/auth/posts/${postSeq}`),

  // ─── 댓글 ───────────────────────────────────────────
  getComments: (postSeq: number) =>
    client
      .get<{ data: CommentResponseDto[] }>(`/posts/${postSeq}/comments`)
      .then((r) => r.data.data),

  createComment: (postSeq: number, content: string, parentCommentSeq?: number) =>
    client
      .post<{ data: number }>(`/auth/posts/${postSeq}/comments`, {
        content,
        parentCommentSeq: parentCommentSeq ?? null,
      })
      .then((r) => r.data.data),

  updateComment: (postSeq: number, commentSeq: number, content: string) =>
    client.patch(`/auth/posts/${postSeq}/comments/${commentSeq}`, { content }),

  deleteComment: (postSeq: number, commentSeq: number) =>
    client.delete(`/auth/posts/${postSeq}/comments/${commentSeq}`),

  // ─── 좋아요 ───────────────────────────────────────────
  getLikeStatus: (postSeq: number) =>
    client
      .get<{ data: LikeStatusResponseDto }>(`/likes/posts/${postSeq}`)
      .then((r) => r.data.data),

  toggleLike: (postSeq: number, actionType: 'LIKE' | 'UNLIKE') =>
    client
      .post<{ data: LikePostResponseDto }>('/auth/likes', { postSeq, actionType })
      .then((r) => r.data.data),
}
