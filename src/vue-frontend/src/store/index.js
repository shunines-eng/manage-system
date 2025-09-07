import Vue from 'vue'
import Vuex from 'vuex'
import axios from 'axios'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo')) || null,
    isLoggedIn: !!localStorage.getItem('token')
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      state.isLoggedIn = !!token
      localStorage.setItem('token', token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },
    LOGOUT(state) {
      state.token = ''
      state.userInfo = null
      state.isLoggedIn = false
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    }
  },
  actions: {
    async login({ commit }, credentials) {
      try {
        const response = await axios.post('/auth/login', credentials)
        const { token, user } = response.data
        
        commit('SET_TOKEN', token)
        commit('SET_USER_INFO', user)
        
        return response.data
      } catch (error) {
        throw error
      }
    },
    async register({ dispatch }, userData) {
      try {
        await axios.post('/auth/register', userData)
        // 注册成功后自动登录
        return await dispatch('login', {
          username: userData.username,
          password: userData.password
        })
      } catch (error) {
        throw error
      }
    },
    logout({ commit }) {
      commit('LOGOUT')
    },
    // 刷新用户信息
    async refreshUserInfo({ commit, state }) {
      if (!state.token) {
        return
      }
      
      try {
        const response = await axios.get('/api/user/profile')
        commit('SET_USER_INFO', response.data)
      } catch (error) {
        // 如果获取用户信息失败，可能是token过期，执行登出
        commit('LOGOUT')
      }
    },
    
    // 获取所有用户（管理员功能）
    async getUsers({ state }) {
      if (!state.token) {
        throw new Error('未登录')
      }
      
      try {
        const response = await axios.get('/api/admin/users')
        return response.data
      } catch (error) {
        throw error
      }
    },
    
    // 更新用户信息（管理员功能）
    async updateUser({ state }, { userId, userData }) {
      if (!state.token) {
        throw new Error('未登录')
      }
      
      try {
        const response = await axios.put(`/api/admin/users/${userId}`, userData)
        return response.data
      } catch (error) {
        throw error
      }
    },
    
    // 删除用户（管理员功能）
    async deleteUser({ state }, userId) {
      if (!state.token) {
        throw new Error('未登录')
      }
      
      try {
        const response = await axios.delete(`/api/admin/users/${userId}`)
        return response.data
      } catch (error) {
        throw error
      }
    }
  },
  getters: {
    token: state => state.token,
    userInfo: state => state.userInfo,
    isLoggedIn: state => state.isLoggedIn,
    username: state => state.userInfo?.username || '',
    fullName: state => state.userInfo?.fullName || '',
    role: state => state.userInfo?.role || 'ROLE_USER',
    isAdmin: state => state.userInfo?.role === 'ROLE_ADMIN'
  }
})