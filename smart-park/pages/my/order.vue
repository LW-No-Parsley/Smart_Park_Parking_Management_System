<template>
	<view class="page-order">
		<!-- 状态标签 -->
		<view class="tabs">
			<view class="tab" v-for="t in tabs" :key="t.value" :class="{ active: currentTab === t.value }" @tap="switchTab(t.value)">
				<text>{{ t.label }}</text>
			</view>
		</view>

		<!-- 预约列表 -->
		<view class="order-list" v-if="orderList.length">
			<view class="order-item" v-for="item in orderList" :key="item.id">
				<view class="order-header">
					<text class="order-plate">{{ item.plateNumber || '未知车牌' }}</text>
					<text class="order-status" :class="statusClass(item)">{{ statusText(item) }}</text>
				</view>
				<view class="order-body">
					<view class="order-row">
						<text class="row-label">车位</text>
						<text class="row-value">{{ item.spaceNumber || '-' }}</text>
					</view>
					<view class="order-row">
						<text class="row-label">开始时间</text>
						<text class="row-value">{{ formatTime(item.startTime) }}</text>
					</view>
					<view class="order-row">
						<text class="row-label">结束时间</text>
						<text class="row-value">{{ formatTime(item.endTime) }}</text>
					</view>
					<view class="order-row" v-if="item.totalFee !== undefined && item.totalFee !== null">
						<text class="row-label">费用</text>
						<text class="row-value fee">¥{{ item.totalFee }}</text>
					</view>
				</view>
				<view class="order-footer" v-if="canCancel(item)">
					<text class="cancel-btn" @tap="cancelOrder(item)">取消预约</text>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view class="empty-state" v-else>
			<icon-font class="empty-icon" name="order" size="80rpx" color="#ddd"></icon-font>
			<text class="empty-text">暂无{{ currentTabTitle }}订单</text>
		</view>
	</view>
</template>

<script>
	import request from '../../common/request'

	const STATUS_MAP = {
		0: '已取消',
		1: '已预约',
		2: '已使用',
		3: '已过期'
	}

	export default {
		data() {
			return {
				tabs: [
					{ label: '全部', value: -1 },
					{ label: '已预约', value: 1 },
					{ label: '已使用', value: 2 },
					{ label: '已取消', value: 0 }
				],
				currentTab: -1,
				allOrders: [],
				orderList: []
			}
		},
		onShow() {
			this.loadOrders()
		},
		computed: {
			currentTabTitle() {
				const t = this.tabs.find(t => t.value === this.currentTab)
				return t ? t.label : ''
			}
		},
		methods: {
			getUserId() {
				try {
					const info = JSON.parse(uni.getStorageSync('park_user_info') || '{}')
					return info.id
				} catch { return null }
			},

			loadOrders() {
				if (!this.checkLogin()) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					return uni.navigateBack()
				}
				uni.showLoading({ title: '加载中...', mask: true })
				request.get('/app/reservation/my').then((res) => {
					this.allOrders = Array.isArray(res) ? res : (res || [])
					this.filterOrders()
					uni.hideLoading()
				}).catch(() => {
					this.allOrders = []
					this.orderList = []
					uni.hideLoading()
				})
			},

			filterOrders() {
				if (this.currentTab === -1) {
					this.orderList = [...this.allOrders]
				} else {
					this.orderList = this.allOrders.filter(o => o.status === this.currentTab)
				}
			},

			switchTab(value) {
				this.currentTab = value
				this.filterOrders()
			},

			statusText(item) {
				return STATUS_MAP[item.status] || '未知'
			},

			statusClass(item) {
				switch (item.status) {
					case 0: return 'status-cancelled'
					case 1: return 'status-active'
					case 2: return 'status-used'
					case 3: return 'status-expired'
					default: return ''
				}
			},

			canCancel(item) {
				return item.status === 1
			},

			formatTime(t) {
				if (!t) return '-'
				const d = new Date(t)
				const pad = n => String(n).padStart(2, '0')
				return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
			},

			cancelOrder(item) {
				uni.showModal({
					title: '提示',
					content: '确定要取消该预约吗？',
					success: (res) => {
						if (res.confirm) {
							uni.showLoading({ title: '取消中...', mask: true })
							request.post('/app/reservation/' + item.id + '/cancel').then(() => {
								uni.hideLoading()
								uni.showToast({ title: '已取消', icon: 'success' })
								this.loadOrders()
							}).catch((err) => {
								uni.hideLoading()
								const msg = err.code === 409 ? '该预约数据已被修改，请刷新后重试' : (err.message || '取消失败')
								uni.showToast({ title: msg, icon: 'none' })
							})
						}
					}
				})
			}
		}
	}
</script>

<style lang="scss">
.page-order {
	min-height: 100vh;
	background: #f5f5f5;
}

.tabs {
	display: flex;
	background: #fff;
	padding: 20rpx 30rpx;
	border-bottom: 1rpx solid #f0f0f0;
}
.tab {
	font-size: 28rpx;
	color: #666;
	padding: 8rpx 24rpx;
	margin-right: 16rpx;
	border-radius: 30rpx;
	&.active {
		color: #fff;
		background: #3B86FF;
	}
}

.order-list {
	padding: 20rpx 30rpx;
}
.order-item {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.order-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
	padding-bottom: 16rpx;
	border-bottom: 1rpx solid #f5f5f5;
}
.order-plate {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
}
.order-status {
	font-size: 24rpx;
	padding: 4rpx 16rpx;
	border-radius: 4rpx;
	&.status-active { color: #3B86FF; background: #F0F4FF; }
	&.status-used { color: #4CAF50; background: #E8F5E9; }
	&.status-cancelled { color: #999; background: #f5f5f5; }
	&.status-expired { color: #FF9800; background: #FFF3E0; }
}
.order-body {
	.order-row {
		display: flex;
		justify-content: space-between;
		margin-bottom: 12rpx;
		.row-label { font-size: 24rpx; color: #999; }
		.row-value { font-size: 24rpx; color: #333; &.fee { color: #FF6B35; font-weight: 500; } }
	}
}
.order-footer {
	margin-top: 16rpx;
	padding-top: 16rpx;
	border-top: 1rpx solid #f5f5f5;
	text-align: right;
	.cancel-btn {
		font-size: 24rpx;
		color: #F44336;
		padding: 6rpx 20rpx;
		border: 1rpx solid #F44336;
		border-radius: 6rpx;
	}
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 160rpx 0;
	.empty-icon { font-size: 80rpx; }
	.empty-text { font-size: 26rpx; color: #999; margin-top: 20rpx; }
}
</style>
