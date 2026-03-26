import client from './client'
import type { MemberDto } from '../types'

export const authApi = {
  getMe: () =>
    client.get<{ data: MemberDto }>('/auth/members/me').then((r) => r.data.data),

  logout: () =>
    client.post('/auth/logout'),
}
