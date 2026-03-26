
import axios from 'axios'
import { ElMessage } from 'element-plus'
// 区分开发和生产环境
const DEV_BASE_URL = "http://localhost:8124";
const PROD_BASE_URL = "http://111.229.125.52:81";
// 创建 axios 实例
const request = axios.create({
  // 这里打包上传到云需要修改
  // baseURL: PROD_BASE_URL,
  baseURL: DEV_BASE_URL,
  timeout: 10000, // 请求超时时间
  withCredentials: true,

})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 从 localStorage 获取 token
    const token = localStorage.getItem('token')
    if (token) {
      // 直接使用 token，不添加 Bearer 前缀，因为后端期望的是纯 token
      config.headers.Authorization = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    // 如果响应中的 code 不为 0，则显示错误消息
    if (response.data.code !== 0) {
      ElMessage.error(response.data.message || '请求失败')
      return Promise.reject(response.data)
    }
    return response.data
  },
  (error) => {
    // 处理 HTTP 错误
    let message = '请求失败'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请登录'
          // 清除无效的 token
          localStorage.removeItem('token')
          // 可以在这里处理登出逻辑
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = ' 服务器内部错误'
          break
        default:
          message = error.response.data.message || '请求失败'
      }
    } else if (error.request) {
      message = '网络错误，请检查网络连接'
    }
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default request
