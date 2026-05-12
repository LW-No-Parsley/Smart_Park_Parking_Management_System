<template>
	<view class="page-my">
		<!-- 用户信息 -->
		<view class="user-section">
			<block v-if="isLogin">
				<image class="user-avatar" :src="userInfo.avatar" v-if="userInfo.avatar"></image>
				<view class="user-avatar avatar-placeholder" v-else>
					<icon-font class="avatar-icon" name="user" size="56rpx" color="rgba(255,255,255,0.6)"></icon-font>
				</view>
				<view class="user-info">
					<text class="user-name">{{ userInfo.username || '微信用户' }}</text>
					<text class="user-phone">{{ userInfo.phone ? '已绑定手机' : '已登录' }}</text>
				</view>
			</block>
			<block v-else>
				<view class="user-avatar avatar-placeholder">
					<icon-font class="avatar-icon" name="user" size="56rpx" color="rgba(255,255,255,0.6)"></icon-font>
				</view>
				<view class="user-info" @tap="doLogin">
					<text class="user-name">点击登录</text>
					<text class="user-phone">登录享受更多服务</text>
				</view>
			</block>
			<text class="arrow-icon">&gt;</text>
		</view>

		<!-- 数据统计 -->
		<view class="stats-section">
			<view class="stat-item">
				<text class="stat-num">{{ isLogin ? userInfo.parkCount || 0 : '-' }}</text>
				<text class="stat-label">停车次数</text>
			</view>
			<view class="stat-item">
				<text class="stat-num">{{ isLogin ? '¥' + (userInfo.totalSpend || 0) : '-' }}</text>
				<text class="stat-label">累计消费</text>
			</view>
			<view class="stat-item">
				<text class="stat-num">{{ isLogin ? userInfo.couponCount || 0 : '-' }}</text>
				<text class="stat-label">优惠券</text>
			</view>
		</view>

		<!-- 功能列表 -->
		<view class="menu-section">
			<view class="menu-item" @tap="goOrder">
				<icon-font class="menu-icon" name="order" size="36rpx"></icon-font>
				<text class="menu-label">我的订单</text>
				<icon-font class="menu-arrow" name="arrow-right" size="28rpx" color="#ccc"></icon-font>
			</view>
			<view class="menu-item" @tap="goCar">
				<icon-font class="menu-icon" name="car" size="36rpx"></icon-font>
				<text class="menu-label">车辆管理</text>
				<icon-font class="menu-arrow" name="arrow-right" size="28rpx" color="#ccc"></icon-font>
			</view>
			<view class="menu-item" @tap="goWallet">
				<icon-font class="menu-icon" name="wallet" size="36rpx"></icon-font>
				<text class="menu-label">我的钱包</text>
				<icon-font class="menu-arrow" name="arrow-right" size="28rpx" color="#ccc"></icon-font>
			</view>
			<view class="menu-item" @tap="goCoupon">
				<icon-font class="menu-icon" name="coupon" size="36rpx"></icon-font>
				<text class="menu-label">优惠券</text>
				<icon-font class="menu-arrow" name="arrow-right" size="28rpx" color="#ccc"></icon-font>
			</view>
		</view>

		<!-- 设置列表 -->
		<view class="menu-section">
			<view class="menu-item" @tap="goMessage">
				<icon-font class="menu-icon" name="notification" size="36rpx"></icon-font>
				<text class="menu-label">消息通知</text>
				<icon-font class="menu-arrow" name="arrow-right" size="28rpx" color="#ccc"></icon-font>
			</view>
			<view class="menu-item" @tap="goAbout">
				<icon-font class="menu-icon" name="info" size="36rpx"></icon-font>
				<text class="menu-label">关于我们</text>
				<icon-font class="menu-arrow" name="arrow-right" size="28rpx" color="#ccc"></icon-font>
			</view>
			<view class="menu-item" v-if="isLogin" @tap="logout">
				<icon-font class="menu-icon" name="logout" size="36rpx"></icon-font>
				<text class="menu-label logout-text">退出登录</text>
			</view>
		</view>
	</view>
</template>

<script>
	import request, { saveTokens, clearTokens, getAccessToken } from '../../common/request'

	export default {
		data() {
			return {
				isLogin: false,
				userInfo: {}
			}
		},
		onShow() {
			this.checkLogin()
		},
		methods: {
			// 检查登录状态
			checkLogin() {
				const token = getAccessToken()
				const saved = uni.getStorageSync('park_user_info')

				if (token && saved) {
					try {
						const info = JSON.parse(saved)
						this.isLogin = true
						this.userInfo = info
					} catch (e) {
						this.isLogin = false
						this.userInfo = {}
					}
				} else {
					this.isLogin = false
					this.userInfo = {}
				}
			},

			// 微信登录
			doLogin() {
				uni.showLoading({ title: '登录中...', mask: true })

				uni.login({
					provider: 'weixin',
					success: (loginRes) => {
						// 获取用户信息，然后调用后端登录
						this._getUserInfo(loginRes.code)
					},
					fail: (err) => {
						uni.hideLoading()
						if (!err.errMsg?.includes('no provider') && !err.errMsg?.includes('not supported')) {
							uni.showToast({ title: '登录失败，请重试', icon: 'none' })
							return
						}
						// H5 或无微信环境，游客模式
						this._mockLogin()
					}
				})
			},

			// 获取微信用户信息，然后请求后端登录
			_getUserInfo(code) {
				uni.getUserInfo({
					provider: 'weixin',
					success: (res) => {
						const info = res.userInfo || {}
						this._apiLogin(code, {
							username: info.nickName || '微信用户',
							avatar: info.avatarUrl || ''
						})
					},
					fail: () => {
						// 新版微信未授权也能用 code 登录
						this._apiLogin(code, {
							username: '微信用户',
							avatar: ''
						})
					}
				})
			},

			// 调用后端登录 API
			_apiLogin(code, userInfo) {
				request.post('/app/login', {
					openid: code,
					username: userInfo.username,
					avatar: userInfo.avatar
				}).then((res) => {
					// 保存 Token
					saveTokens(res.accessToken, res.refreshToken)

					// 保存用户信息
					const user = res.user || {}
					this._saveUserInfo({
						id: user.id,
						username: user.username || userInfo.username,
						avatar: user.avatar || userInfo.avatar || '',
						phone: user.phone || '',
						userType: user.userType || 2,
						parkCount: 0,
						totalSpend: 0,
						couponCount: 0
					})
					uni.hideLoading()
					uni.showToast({ title: '登录成功', icon: 'success' })
				}).catch((err) => {
					uni.hideLoading()
					// 后端不可用时降级到本地登录
					if (err.errMsg?.includes('timeout') || err.errMsg?.includes('request:fail')) {
						this._localLogin(userInfo)
					} else {
						uni.showToast({ title: err.message || '登录失败', icon: 'none' })
					}
				})
			},

			// 本地降级登录（后端不可用时）
			_localLogin(userInfo) {
				this._saveUserInfo({
					username: userInfo.username || '微信用户',
					avatar: userInfo.avatar || '',
					parkCount: 0,
					totalSpend: 0,
					couponCount: 0
				})
				uni.showToast({ title: '已离线登录', icon: 'success' })
			},

			// 游客模式（H5 环境）
			_mockLogin() {
				this._saveUserInfo({
					username: '游客',
					avatar: '',
					parkCount: 0,
					totalSpend: 0,
					couponCount: 0
				})
				uni.hideLoading()
				uni.showToast({ title: '登录成功（游客模式）', icon: 'success' })
			},

			// 保存用户信息
			_saveUserInfo(info) {
				this.userInfo = info
				this.isLogin = true
				uni.setStorageSync('park_user_info', JSON.stringify(info))
			},

			// 退出登录
			logout() {
				uni.showModal({
					title: '提示',
					content: '确定要退出登录吗？',
					success: (res) => {
						if (res.confirm) {
							clearTokens()
							uni.removeStorageSync('park_user_info')
							this.isLogin = false
							this.userInfo = {}
							uni.showToast({ title: '已退出', icon: 'success' })
						}
					}
				})
			},

			goOrder() { uni.navigateTo({ url: '/pages/my/order' }) },
			goCar() { uni.navigateTo({ url: '/pages/my/car' }) },
			goWallet() { uni.showToast({ title: '功能开发中', icon: 'none' }) },
			goCoupon() { uni.showToast({ title: '功能开发中', icon: 'none' }) },
			goMessage() { uni.showToast({ title: '功能开发中', icon: 'none' }) },
			goAbout() { uni.showToast({ title: '功能开发中', icon: 'none' }) }
		}
	}
</script>

<style lang="scss">
.page-my {
	min-height: 100vh;
	background: #f5f5f5;
	padding-bottom: 20rpx;
}

/* 用户信息 */
.user-section {
	background: linear-gradient(135deg, #3B86FF, #6AA5FF);
	padding: 60rpx 40rpx 50rpx;
	display: flex;
	align-items: center;
	color: #fff;
}
.user-avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 50%;
	flex-shrink: 0;
}
.avatar-placeholder {
	background: rgba(255,255,255,0.3);
	display: flex;
	align-items: center;
	justify-content: center;
	.avatar-icon {
		font-size: 56rpx;
	}
}
.user-info {
	flex: 1;
	margin-left: 24rpx;
	min-width: 0;
}
.user-name {
	font-size: 36rpx;
	font-weight: 600;
	color: #fff;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
}
.user-phone {
	font-size: 24rpx;
	color: rgba(255,255,255,0.8);
	margin-top: 8rpx;
	display: block;
}
.arrow-icon {
	font-size: 32rpx;
	color: rgba(255,255,255,0.6);
}

/* 数据统计 */
.stats-section {
	display: flex;
	background: #fff;
	padding: 30rpx 0;
	margin: 0 0 20rpx;
}
.stat-item {
	flex: 1;
	text-align: center;
	border-right: 1rpx solid #f0f0f0;
	&:last-child { border-right: none; }
}
.stat-num {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}
.stat-label {
	font-size: 24rpx;
	color: #999;
	margin-top: 8rpx;
	display: block;
}

/* 菜单 */
.menu-section {
	background: #fff;
	margin: 0 0 20rpx;
	padding: 0 30rpx;
}
.menu-item {
	display: flex;
	align-items: center;
	padding: 28rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
	&:last-child { border-bottom: none; }
}
.menu-icon {
	font-size: 36rpx;
	margin-right: 20rpx;
}
.menu-label {
	flex: 1;
	font-size: 28rpx;
	color: #333;
}
.logout-text {
	color: #F44336;
}
.menu-arrow {
	font-size: 28rpx;
	color: #ccc;
}
</style>
