import { create } from 'zustand'
import type { MemberDto } from '../types'
import { authApi } from '../api/auth'

interface AuthState {
  member: MemberDto | null
  isLoading: boolean
  isLoggedIn: boolean
  fetchMe: () => Promise<void>
  logout: () => Promise<void>
  clear: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  member: null,
  isLoading: true,
  isLoggedIn: false,

  fetchMe: async () => {
    try {
      set({ isLoading: true })
      const member = await authApi.getMe()
      set({ member, isLoggedIn: true, isLoading: false })
    } catch {
      set({ member: null, isLoggedIn: false, isLoading: false })
    }
  },

  logout: async () => {
    await authApi.logout()
    set({ member: null, isLoggedIn: false })
    window.location.href = '/login'
  },

  clear: () => set({ member: null, isLoggedIn: false }),
}))
