<template>
	<view class="page-map">
		<!-- 地图组件 -->
		<map
			id="parkMap"
			ref="parkMap"
			class="map-container"
			:latitude="currentLat"
			:longitude="currentLng"
			:markers="markers"
			:show-location="true"
			:scale="scale"
			:style="{ width: '100%', height: mapHeight }"
			@markertap="handleMarkerTap"
			@callouttap="handleCalloutTap"
		/>

		<!-- 定位按钮 -->
		<cover-view class="location-btn" @tap="getLocation">
			<cover-image class="loc-icon" src="/static/map/locate.png" />
		</cover-view>

		<!-- 选中停车场详情浮层 -->
		<view class="detail-popup" v-if="selectedPark" @tap="goDetail(selectedPark)">
			<view class="popup-info">
				<text class="popup-name">{{ selectedPark.name }}</text>
				<view class="popup-tags" v-if="selectedPark.tags && selectedPark.tags.length">
					<text class="popup-tag" v-for="(t, i) in selectedPark.tags" :key="i">{{ t }}</text>
				</view>
				<view class="popup-meta">
					<text class="popup-addr">{{ selectedPark.address }}</text>
					<text class="popup-price" v-if="selectedPark.price">{{ selectedPark.price }}</text>
				</view>
			</view>
			<view class="popup-status" :class="selectedPark.available > 10 ? 'green' : selectedPark.available > 0 ? 'orange' : 'red'">
				<text class="popup-num">{{ selectedPark.available }}</text>
				<text class="popup-label">空位</text>
			</view>
		</view>

		<!-- 底部面板 -->
		<view class="bottom-panel" :class="{ 'panel-fold': panelFolded }">
			<!-- 折叠手柄 -->
			<view class="panel-handle" @tap="togglePanel">
				<view class="handle-bar"></view>
			</view>

			<!-- 面板头部 -->
			<view class="panel-header">
				<text class="panel-title">附近停车场</text>
				<text class="panel-count">共 {{ parkList.length }} 个</text>
			</view>

			<!-- 列表 -->
			<scroll-view class="panel-list" scroll-y :show-scrollbar="false">
				<view
					class="list-item"
					v-for="(item, index) in parkList"
					:key="item.id"
					:class="{ active: selectedPark && selectedPark.id === item.id }"
					@tap="selectFromList(item)"
				>
					<view class="item-left">
						<view class="item-name-row">
							<text class="item-name">{{ item.name }}</text>
							<text class="item-tag" v-if="item.tag">{{ item.tag }}</text>
						</view>
						<text class="item-addr">{{ item.address }}</text>
						<view class="item-meta">
							<text class="meta-distance" v-if="item.distance">{{ item.distance }}</text>
							<text class="meta-price" v-if="item.price">{{ item.price }}</text>
						</view>
					</view>
					<view class="item-right">
						<text class="item-spaces" :class="item.available > 10 ? 'green' : item.available > 0 ? 'orange' : 'red'">
							{{ item.available }}
						</text>
						<text class="item-label">空位</text>
					</view>
				</view>
			</scroll-view>
		</view>
	</view>
</template>

<script>
	import request from '../../common/request'

	export default {
		data() {
			return {
				currentLat: 31.2304,
				currentLng: 121.4737,
				scale: 15,
				parkList: [],
				markers: [],
				selectedPark: null,
				panelFolded: false,
				mapHeight: 'calc(100vh - 100rpx)',
				mapCtx: null
			}
		},
		onLoad() {
			this.loadParkList()
		},
		onReady() {
			this.mapCtx = uni.createMapContext('parkMap', this)
		},
		methods: {
			loadParkList() {
				request.get('/app/park-area/list').then((list) => {
					const parks = Array.isArray(list) ? list : []
					const promises = parks.map(p =>
						request.get('/app/park-area/' + p.id + '/occupancy-stats')
							.then(stats => ({
								...p,
								available: stats.availableSpaces ?? 0,
								distance: '',
								price: '',
								tags: [],
								tag: ''
							}))
							.catch(() => ({
								...p,
								available: p.totalSpaces || 0,
								distance: '',
								price: '',
								tags: [],
								tag: ''
							}))
					)
					return Promise.all(promises)
				}).then((parks) => {
					this.parkList = parks
					this.getLocation()
				}).catch(() => {
					this.parkList = []
					this.getLocation()
				})
			},

			// 构建地图标注
			buildMarkers() {
				this.markers = this.parkList.map((item, index) => ({
					id: item.id,
					latitude: item.latitude,
					longitude: item.longitude,
					iconPath: '/static/map/marker.png',
					width: 28,
					height: 34,
					callout: {
						content: `${item.name}\n${item.available} 空位`,
						fontSize: 12,
						borderRadius: 6,
						padding: 8,
						bgColor: '#ffffff',
						borderColor: '#3B86FF',
						borderWidth: 1,
						display: 'BYCLICK',
						textAlign: 'center'
					}
				}))
			},

			// 获取用户定位
			getLocation() {
				uni.showLoading({ title: '定位中...' })
				this._doGetLocation('gcj02')
			},

			_doGetLocation(type) {
				uni.getLocation({
					type: type,
					success: (res) => {
						this.currentLat = res.latitude
						this.currentLng = res.longitude
						this.buildMarkers()
						this.$nextTick(() => {
							this.fitToMarkers()
						})
						uni.hideLoading()
					},
					fail: (err) => {
						if (type === 'gcj02') {
							console.warn('gcj02 定位失败，降级到 wgs84:', err)
							this._doGetLocation('wgs84')
						} else {
							console.error('定位失败:', err)
							uni.showToast({
								title: err.errMsg?.includes('deny') ? '请允许定位权限' : '定位失败，使用默认位置',
								icon: 'none'
							})
							this.buildMarkers()
							uni.hideLoading()
						}
					}
				})
			},

			// 视图适配所有标注
			fitToMarkers() {
				if (!this.mapCtx) return
				const points = this.parkList.map(p => ({
					latitude: p.latitude,
					longitude: p.longitude
				}))
				points.push({
					latitude: this.currentLat,
					longitude: this.currentLng
				})
				this.mapCtx.includePoints({
					points,
					padding: [60, 40, 300, 40]
				})
			},

			// 点击标注
			handleMarkerTap(e) {
				const id = e.detail.markerId
				const park = this.parkList.find(p => p.id === id)
				if (park) {
					this.selectedPark = park
				}
			},

			// 点击标注气泡
			handleCalloutTap(e) {
				const id = e.detail.markerId
				const park = this.parkList.find(p => p.id === id)
				if (park) {
					this.goDetail(park)
				}
			},

			// 从列表选中停车场
			selectFromList(item) {
				this.selectedPark = item
				this.currentLat = item.latitude
				this.currentLng = item.longitude
				this.markers = this.markers.map(m => ({
					...m,
					iconPath: m.id === item.id ? '/static/map/marker-active.png' : '/static/map/marker.png'
				}))
			},

			// 切换面板折叠
			togglePanel() {
				this.panelFolded = !this.panelFolded
				this.$nextTick(() => {
					this.fitToMarkers()
				})
			},

			// 跳转详情
			goDetail(item) {
				uni.navigateTo({ url: '/pages/park/detail?parkId=' + item.id })
			}
		}
	}
</script>

<style lang="scss">
.page-map {
	position: relative;
	width: 100%;
	height: 100vh;
	overflow: hidden;
}

.map-container {
	width: 100%;
	height: 100%;
}

/* 定位按钮 */
.location-btn {
	position: absolute;
	top: 20rpx;
	right: 20rpx;
	width: 68rpx;
	height: 68rpx;
	background: #fff;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.15);
	z-index: 10;
}
.loc-icon {
	width: 36rpx;
	height: 36rpx;
}

/* 详情浮层 */
.detail-popup {
	position: absolute;
	bottom: 380rpx;
	left: 30rpx;
	right: 30rpx;
	background: #fff;
	border-radius: 16rpx;
	padding: 24rpx 30rpx;
	display: flex;
	align-items: center;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.12);
	z-index: 10;
}
.popup-info {
	flex: 1;
	margin-right: 20rpx;
}
.popup-name {
	font-size: 30rpx;
	font-weight: 600;
	color: #333;
}
.popup-tags {
	display: flex;
	gap: 8rpx;
	margin-top: 6rpx;
}
.popup-tag {
	font-size: 20rpx;
	color: #3B86FF;
	background: #F0F4FF;
	padding: 2rpx 12rpx;
	border-radius: 4rpx;
}
.popup-meta {
	display: flex;
	gap: 20rpx;
	margin-top: 10rpx;
}
.popup-addr {
	font-size: 22rpx;
	color: #999;
}
.popup-price {
	font-size: 24rpx;
	color: #FF6B35;
}
.popup-status {
	text-align: center;
	min-width: 80rpx;
	.popup-num {
		font-size: 40rpx;
		font-weight: bold;
	}
	.popup-label {
		font-size: 20rpx;
	}
	&.green .popup-num, &.green .popup-label { color: #4CAF50; }
	&.orange .popup-num, &.orange .popup-label { color: #FF9800; }
	&.red .popup-num, &.red .popup-label { color: #F44336; }
}

/* 底部面板 */
.bottom-panel {
	position: absolute;
	bottom: 0;
	left: 0;
	right: 0;
	background: #fff;
	border-radius: 24rpx 24rpx 0 0;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.08);
	z-index: 20;
	max-height: 45vh;
	transition: transform 0.3s ease;

	&.panel-fold {
		transform: translateY(calc(100% - 80rpx));
		.panel-list { height: 0; overflow: hidden; }
	}
}

.panel-handle {
	display: flex;
	justify-content: center;
	padding: 16rpx 0 8rpx;
	.handle-bar {
		width: 56rpx;
		height: 6rpx;
		background: #ddd;
		border-radius: 3rpx;
	}
}

.panel-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 0 30rpx 16rpx;
}
.panel-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
}
.panel-count {
	font-size: 24rpx;
	color: #999;
}

.panel-list {
	max-height: calc(45vh - 100rpx);
	padding: 0 30rpx 20rpx;
}

.list-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 22rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
	&:last-child { border-bottom: none; }
	&.active {
		background: #F8FBFF;
		margin: 0 -20rpx;
		padding: 22rpx 20rpx;
		border-radius: 12rpx;
	}
}
.item-name-row {
	display: flex;
	align-items: center;
	gap: 10rpx;
}
.item-name {
	font-size: 26rpx;
	font-weight: 600;
	color: #333;
}
.item-tag {
	font-size: 20rpx;
	color: #3B86FF;
	background: #F0F4FF;
	padding: 2rpx 12rpx;
	border-radius: 4rpx;
}
.item-addr {
	font-size: 22rpx;
	color: #999;
	margin-top: 6rpx;
	display: block;
}
.item-meta {
	display: flex;
	gap: 20rpx;
	margin-top: 8rpx;
	.meta-distance, .meta-price {
		font-size: 22rpx;
		color: #666;
	}
	.meta-price {
		color: #FF6B35;
	}
}
.item-right {
	text-align: center;
	min-width: 72rpx;
}
.item-spaces {
	font-size: 36rpx;
	font-weight: bold;
	&.green { color: #4CAF50; }
	&.orange { color: #FF9800; }
	&.red { color: #F44336; }
}
.item-label {
	font-size: 20rpx;
	color: #999;
	display: block;
}
</style>
