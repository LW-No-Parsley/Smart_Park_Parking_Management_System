<template>
	<view class="page-home">
		<!-- 顶部搜索栏 -->
		<view class="search-bar">
			<view class="search-input" @tap="goSearch">
				<icon-font name="search" size="32rpx" color="#999"></icon-font>
				<text class="placeholder">搜索停车场/位置</text>
			</view>
		</view>

		<!-- 快捷功能区 -->
		<view class="quick-actions">
			<view class="action-item" @tap="goMap">
				<view class="action-icon bg-nearby"><icon-font name="nearby" size="40rpx"></icon-font></view>
				<text class="action-text">附近车场</text>
			</view>
			<view class="action-item" @tap="goHistory">
				<view class="action-icon bg-history"><icon-font name="history" size="40rpx"></icon-font></view>
				<text class="action-text">历史记录</text>
			</view>
			<view class="action-item" @tap="goFavorite">
				<view class="action-icon bg-fav"><icon-font name="favorite" size="40rpx"></icon-font></view>
				<text class="action-text">常用车位</text>
			</view>
			<view class="action-item" @tap="goCharge">
				<view class="action-icon bg-charge"><icon-font name="charge" size="40rpx"></icon-font></view>
				<text class="action-text">充电桩</text>
			</view>
		</view>

		<!-- 推荐停车场 -->
		<view class="section">
			<view class="section-header">
				<text class="section-title">推荐停车场</text>
				<text class="section-more" @tap="goMap">查看更多 &gt;</text>
			</view>
			<view class="park-list">
				<view class="park-card" v-for="(item, index) in parkList" :key="index" @tap="goDetail(item)">
					<view class="park-info">
						<text class="park-name">{{ item.name }}</text>
						<view class="park-tags" v-if="item.tags && item.tags.length">
							<text class="tag" v-for="(tag, ti) in item.tags" :key="ti">{{ tag }}</text>
						</view>
						<view class="park-address">
							<text class="addr-text">{{ item.address }}</text>
						</view>
						<view class="park-meta">
							<text class="park-distance" v-if="item.distance">{{ item.distance }}</text>
							<text class="park-hours" v-else-if="item.businessHoursStart">{{ item.businessHoursStart.slice(0,5) }}-{{ item.businessHoursEnd.slice(0,5) }}</text>
							<text class="park-price" v-if="item.price">{{ item.price }}</text>
							<text class="park-total" v-else>总{{ item.totalSpaces || '?' }}车位</text>
						</view>
					</view>
					<view class="park-status" :class="item.statusClass">
						<text class="status-num">{{ item.available }}</text>
						<text class="status-label">空位</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import request from '../../common/request'

	export default {
		data() {
			return {
				parkList: []
			}
		},
		onShow() {
			this.loadParkList()
		},
		onPullDownRefresh() {
			this.loadParkList().finally(() => {
				uni.stopPullDownRefresh()
			})
		},
		methods: {
			loadParkList() {
				return request.get('/app/park-area/list').then((list) => {
					const parks = Array.isArray(list) ? list : []
					const promises = parks.map(p =>
						request.get('/app/park-area/' + p.id + '/occupancy-stats')
							.then(stats => ({ ...p, available: stats.availableSpaces ?? 0 }))
							.catch(() => ({ ...p, available: p.totalSpaces || 0 }))
					)
					return Promise.all(promises)
				}).then((parks) => {
					this.parkList = parks.map(p => ({
						...p,
						statusClass: p.available > 10 ? 'status-many' : p.available > 0 ? 'status-few' : 'status-full'
					}))
				}).catch(() => {
					this.parkList = []
				})
			},

			goSearch() {
				uni.showToast({ title: '搜索功能开发中', icon: 'none' })
			},
			goMap() {
				uni.switchTab({ url: '/pages/map/map' })
			},
			goHistory() {
				uni.showToast({ title: '功能开发中', icon: 'none' })
			},
			goFavorite() {
				uni.showToast({ title: '功能开发中', icon: 'none' })
			},
			goCharge() {
				uni.showToast({ title: '功能开发中', icon: 'none' })
			},
			goDetail(item) {
				uni.navigateTo({ url: '/pages/park/detail?parkId=' + item.id })
			}
		}
	}
</script>

<style lang="scss">
.page-home {
	min-height: 100vh;
	background-color: #f5f5f5;
	padding-bottom: 20rpx;
}

/* 搜索栏 */
.search-bar {
	padding: 20rpx 30rpx;
	background: linear-gradient(135deg, #3B86FF, #6AA5FF);
}
.search-input {
	display: flex;
	align-items: center;
	background: rgba(255, 255, 255, 0.95);
	border-radius: 50rpx;
	padding: 20rpx 30rpx;
	.placeholder {
		color: #999;
		font-size: 28rpx;
		margin-left: 16rpx;
	}
	.icon-search {
		font-size: 32rpx;
	}
}

/* 快捷功能 */
.quick-actions {
	display: flex;
	justify-content: space-around;
	background: #fff;
	padding: 30rpx 20rpx;
	margin: 0 0 20rpx;
}
.action-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}
.action-icon {
	width: 88rpx;
	height: 88rpx;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 40rpx;
	margin-bottom: 12rpx;
}
.bg-nearby { background: #E8F0FE; }
.bg-history { background: #FFF3E0; }
.bg-fav { background: #FFF8E1; }
.bg-charge { background: #E8F5E9; }
.action-text {
	font-size: 24rpx;
	color: #333;
}

/* 推荐列表 */
.section {
	margin: 0 20rpx;
}
.section-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}
.section-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}
.section-more {
	font-size: 26rpx;
	color: #3B86FF;
}

.park-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}
.park-card {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.park-info {
	flex: 1;
	margin-right: 20rpx;
}
.park-name {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
}
.park-tags {
	display: flex;
	gap: 10rpx;
	margin-top: 10rpx;
}
.tag {
	background: #F0F4FF;
	color: #3B86FF;
	font-size: 22rpx;
	padding: 4rpx 14rpx;
	border-radius: 6rpx;
}
.park-address {
	margin-top: 12rpx;
	.addr-text {
		font-size: 24rpx;
		color: #999;
	}
}
.park-meta {
	display: flex;
	justify-content: space-between;
	margin-top: 16rpx;
}
.park-distance {
	font-size: 24rpx;
	color: #666;
}
.park-hours {
	font-size: 24rpx;
	color: #666;
}
.park-price {
	font-size: 26rpx;
	color: #FF6B35;
	font-weight: 500;
}
.park-total {
	font-size: 24rpx;
	color: #999;
}

.park-status {
	text-align: center;
	min-width: 100rpx;
	.status-num {
		font-size: 44rpx;
		font-weight: bold;
	}
	.status-label {
		font-size: 22rpx;
		display: block;
		margin-top: 4rpx;
	}
	&.status-many {
		.status-num { color: #4CAF50; }
		.status-label { color: #4CAF50; }
	}
	&.status-few {
		.status-num { color: #FF9800; }
		.status-label { color: #FF9800; }
	}
	&.status-full {
		.status-num { color: #F44336; }
		.status-label { color: #F44336; }
	}
}
</style>
