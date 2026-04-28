
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet, userLoginUsingPost, userLogoutUsingPost, userRegisterUsingPost } from '@/api/userController'
import type { LoginUserVO, UserLoginRequest, UserRegisterRequest } from '@/api/typings'
import { ElMessage } from 'element-plus'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 用户登录状态
  const loginUser = ref<LoginUserVO | null>(null)

  // 计算属性：是否已登录
  const isLoggedIn = computed(() => !!loginUser.value)

  // 计算属性：用户角色
  const userRole = computed(() => loginUser.value?.userRole || '')

  // 计算属性：是否为管理员
  const isAdmin = computed(() => userRole.value === 'admin')

  // 获取当前登录用户信息
  async function fetchLoginUser() {
    try {
      const res = await getLoginUserUsingGet()
      if (res.data) {
        loginUser.value = res.data
      }
      return res.data
    } catch (error) {
      console.error('获取登录用户信息失败', error)
      return null
    }
  }

  // 用户登录
  async function login(loginForm: UserLoginRequest) {
    try {
      const res = await userLoginUsingPost(loginForm)
      if (res.data) {
        loginUser.value = res.data
        // 保存 token 到 localStorage
        if (res.data.token) {
          localStorage.setItem('token', res.data.token)
        }
        // 保存用户角色
        localStorage.setItem('userRole', res.data.userRole || '')
        ElMessage.success('登录成功')
        // 登录成功后跳转到首页
        router.push('/')
      }
      return res.data
    } catch (error) {
      console.error('登录失败', error)
      ElMessage.error('登录失败，请检查用户名和密码')
      return null
    }
  }

  // 用户注册
  async function register(registerForm: UserRegisterRequest) {
    try {
      const res = await userRegisterUsingPost(registerForm)
      if (res.data) {
        ElMessage.success('注册成功，请登录')
        // 注册成功后跳转到登录页
        router.push('/login')
      }
      return res.data
    } catch (error) {
      console.error('注册失败', error)
      ElMessage.error('注册失败，请检查输入信息')
      return null
    }
  }

  // 用户登出
  async function logout() {
    try {
      await userLogoutUsingPost()
      loginUser.value = null
      // 清除localStorage中的token和用户角色
      localStorage.removeItem('token')
      localStorage.removeItem('userRole')
      ElMessage.success('登出成功')
      // 登出后跳转到首页
      router.push('/')
    } catch (error) {
      console.error('登出失败', error)
      ElMessage.error('登出失败')
    }
  }

  // 初始化用户状态
  async function initUserState() {
    const token = localStorage.getItem('token')
    if (token) {
      try {
        await fetchLoginUser()
      } catch (error: any) {
        // 静默处理错误，不显示错误消息
        console.error('初始化用户状态失败', error)
        // 只有当错误明确表示未登录时，才清除 token
        // 避免因为服务器内部错误导致误退出
        if (error.code === 401 || error.message?.includes('未登录') || error.response?.status === 401) {
          localStorage.removeItem('token')
        }
      }
    }
  }

  return {
    loginUser,
    isLoggedIn,
    userRole,
    isAdmin,
    fetchLoginUser,
    login,
    register,
    logout,
    initUserState
  }
})
