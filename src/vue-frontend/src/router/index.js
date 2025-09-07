import Vue from 'vue'
import Router from 'vue-router'
import store from '../store'

// 懒加载组件
const Login = () => import('@/views/Login.vue')
const Register = () => import('@/views/Register.vue')
const Home = () => import('@/views/Home.vue')
const UserManagement = () => import('@/views/UserManagement.vue')

Vue.use(Router)

const router = new Router({
  mode: 'history',
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: Login,
      meta: {
        requiresAuth: false
      }
    },
    {
      path: '/register',
      name: 'Register',
      component: Register,
      meta: {
        requiresAuth: false
      }
    },
    {
      path: '/',
      name: 'Home',
      component: Home,
      meta: {
        requiresAuth: true
      }
    },
    {
      path: '/user-management',
      name: 'UserManagement',
      component: UserManagement,
      meta: {
        requiresAuth: true,
        requiresAdmin: true
      }
    },
    // 404页面
    {
      path: '*',
      redirect: '/login'
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    // 检查用户是否已登录
    if (store.getters.isLoggedIn) {
      // 检查是否需要管理员权限
      if (to.meta.requiresAdmin) {
        // 检查用户是否是管理员
        if (store.getters.userInfo?.role === 'ROLE_ADMIN') {
          next()
        } else {
          // 不是管理员，重定向到首页
          next({ name: 'Home' })
          Vue.prototype.$message.warning('您没有管理员权限')
        }
      } else {
        // 不需要管理员权限，直接通过
        next()
      }
    } else {
      // 未登录，重定向到登录页
      next({ name: 'Login' })
    }
  } else {
    // 不需要认证的页面，直接通过
    next()
  }
})

export default router