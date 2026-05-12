/**
 * HTTP 请求工具
 * 统一处理：Token 注入、Token 刷新、错误处理
 */
import config from './config'

// Token 存储 key
const ACCESS_TOKEN_KEY = 'park_access_token'
const REFRESH_TOKEN_KEY = 'park_refresh_token'

// 是否正在刷新 Token
let isRefreshing = false
// 等待 Token 刷新的请求队列
let requestQueue = []

/**
 * 获取 accessToken
 */
export function getAccessToken() {
	return uni.getStorageSync(ACCESS_TOKEN_KEY) || ''
}

/**
 * 获取 refreshToken
 */
export function getRefreshToken() {
	return uni.getStorageSync(REFRESH_TOKEN_KEY) || ''
}

/**
 * 保存 Token
 */
export function saveTokens(accessToken, refreshToken) {
	if (accessToken) uni.setStorageSync(ACCESS_TOKEN_KEY, accessToken)
	if (refreshToken) uni.setStorageSync(REFRESH_TOKEN_KEY, refreshToken)
}

/**
 * 清除 Token
 */
export function clearTokens() {
	uni.removeStorageSync(ACCESS_TOKEN_KEY)
	uni.removeStorageSync(REFRESH_TOKEN_KEY)
}

/**
 * 刷新 Token
 */
function refreshTokens() {
	return new Promise((resolve, reject) => {
		const refreshToken = getRefreshToken()
		if (!refreshToken) {
			reject(new Error('无 refreshToken'))
			return
		}

		uni.request({
			url: `${config.baseUrl}/app/refresh`,
			method: 'POST',
			header: {
				'Authorization': `Bearer ${refreshToken}`
			},
			success: (res) => {
				const data = res.data
				if (data.code === 200 && data.data) {
					const newAccess = data.data.accessToken
					const newRefresh = data.data.refreshToken
					saveTokens(newAccess, newRefresh)
					resolve(newAccess)
				} else {
					// 刷新失败，清除 Token
					clearTokens()
					reject(new Error(data.message || 'Token 刷新失败'))
				}
			},
			fail: (err) => {
				reject(err)
			}
		})
	})
}

/**
 * 基础请求方法
 */
function request(options) {
	return new Promise((resolve, reject) => {
		const accessToken = getAccessToken()
		const header = {
			'Content-Type': 'application/json',
			...options.header
		}

		// 注入 accessToken
		if (accessToken && !options.noAuth) {
			header['Authorization'] = `Bearer ${accessToken}`
		}

		uni.request({
			url: `${config.baseUrl}${options.url}`,
			method: options.method || 'GET',
			data: options.data,
			header,
			success: (res) => {
				const data = res.data

				// 401 未授权，尝试刷新 Token
				if (data.code === 401 || res.statusCode === 401) {
					if (!isRefreshing) {
						isRefreshing = true
						refreshTokens().then((newToken) => {
							isRefreshing = false
							// 重放等待队列中的请求
							requestQueue.forEach(cb => cb(newToken))
							requestQueue = []
							// 重试当前请求
							options.header = options.header || {}
							options.header['Authorization'] = `Bearer ${newToken}`
							request(options).then(resolve).catch(reject)
						}).catch((err) => {
							isRefreshing = false
							requestQueue = []
							// Token 刷新失败，跳转登录
							uni.navigateTo({ url: '/pages/my/my' })
							reject(err)
						})
					} else {
						// 正在刷新中，排队等待
						return new Promise((waitResolve) => {
							requestQueue.push((newToken) => {
								options.header['Authorization'] = `Bearer ${newToken}`
								request(options).then(resolve).catch(reject)
								waitResolve()
							})
						})
					}
					return
				}

				// 业务错误
				if (data.code !== 200) {
					uni.showToast({ title: data.message || '请求失败', icon: 'none' })
					reject(data)
					return
				}

				resolve(data.data !== undefined ? data.data : data)
			},
			fail: (err) => {
				uni.showToast({ title: '网络异常，请检查网络连接', icon: 'none' })
				reject(err)
			}
		})
	})
}

export default {
	get(url, params, options) {
		return request({ url, method: 'GET', data: params, ...options })
	},

	post(url, data, options) {
		return request({ url, method: 'POST', data, ...options })
	},

	put(url, data, options) {
		return request({ url, method: 'PUT', data, ...options })
	},

	delete(url, data, options) {
		return request({ url, method: 'DELETE', data, ...options })
	},

	request
}
