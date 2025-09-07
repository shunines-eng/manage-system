import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import axios from 'axios'
import { mapGetters } from 'vuex'

// 注册Vuex辅助函数到全局
Vue.prototype.mapGetters = mapGetters

// 使用ElementUI
Vue.use(ElementUI)

// 配置axios
axios.defaults.baseURL = '/api'

// 请求拦截器 - 添加JWT令牌
axios.interceptors.request.use(
  config => {
    const token = store.getters.token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理认证错误
axios.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (error.response && error.response.status === 401) {
      // 未授权，跳转到登录页
      store.dispatch('logout')
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

// 挂载axios到Vue实例
Vue.prototype.$axios = axios

new Vue({
  el: '#app',
  router,
  store,
  render: h => h(App)
})