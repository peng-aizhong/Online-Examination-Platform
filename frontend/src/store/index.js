import { createPinia } from 'pinia'
import { defineStore } from 'pinia'

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
      this.userInfo = userInfo
      this.token = token
      if (token) {
        localStorage.setItem('token', token)
      }
    },
    
    logout() {
      this.userInfo = null
      this.token = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
    
    loadUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    }
  }
})

const pinia = createPinia()

export default pinia
