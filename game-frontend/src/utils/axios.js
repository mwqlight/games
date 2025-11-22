import axios from 'axios'

// 创建axios实例
const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080/api', // 后端API的基础URL
  timeout: 10000, // 请求超时时间
})

// 请求拦截器
axiosInstance.interceptors.request.use(
  config => {
    // 可以在这里添加token等请求头
    return config
  },
  error => {
    // 请求错误处理
    return Promise.reject(error)
  }
)

// 响应拦截器
axiosInstance.interceptors.response.use(
  response => {
    // 可以在这里统一处理响应数据
    return response
  },
  error => {
    // 响应错误处理
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export default axiosInstance
