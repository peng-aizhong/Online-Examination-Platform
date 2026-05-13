import { createPinia } from 'pinia'
import { defineStore } from 'pinia'

const normalizeRole = (role) => {
  if (!role) return role
  return String(role).toLowerCase()
}

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: localStorage.getItem('token') || null
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    getUserInfo: (state) => state.userInfo
  },

  actions: {
    setUser(userInfo, token) {
      const normalizedUser = userInfo ? { ...userInfo, role: normalizeRole(userInfo.role) } : null
      this.userInfo = normalizedUser
      this.token = token
      if (token) {
        localStorage.setItem('token', token)
      }
      if (normalizedUser) {
        localStorage.setItem('userInfo', JSON.stringify(normalizedUser))
      }
    },

    logout() {
      this.userInfo = null
      this.token = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    loadUserInfo(userInfo) {
      const normalizedUser = userInfo ? { ...userInfo, role: normalizeRole(userInfo.role) } : null
      this.userInfo = normalizedUser
      if (normalizedUser) {
        localStorage.setItem('userInfo', JSON.stringify(normalizedUser))
      }
    },

    restoreFromLocal() {
      const raw = localStorage.getItem('userInfo')
      if (!raw) return
      try {
        const parsed = JSON.parse(raw)
        this.userInfo = parsed ? { ...parsed, role: normalizeRole(parsed.role) } : null
      } catch (e) {
        localStorage.removeItem('userInfo')
      }
    }
  }
})

const pinia = createPinia()

export default pinia
