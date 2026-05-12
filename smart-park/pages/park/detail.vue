<template>
	<view class="page-detail">
		<!-- 园区信息 -->
		<view class="park-header">
			<text class="park-name">{{ park.name }}</text>
			<text class="park-addr">{{ park.address }}</text>
			<view class="park-stats">
				<view class="stat">
					<text class="stat-num">{{ park.totalSpaces || '-' }}</text>
					<text class="stat-label">总车位</text>
				</view>
				<view class="stat">
					<text class="stat-num green">{{ stats.availableSpaces ?? '-' }}</text>
					<text class="stat-label">空闲</text>
				</view>
				<view class="stat">
					<text class="stat-num red">{{ stats.occupiedSpaces ?? '-' }}</text>
					<text class="stat-label">占用</text>
				</view>
			</view>
		</view>

		<!-- 分区选择 -->
		<view class="section">
			<text class="section-title">选择车位</text>
			<view class="zone-tabs" v-if="zones.length">
				<text class="zone-tab" v-for="z in zones" :key="z.id" :class="{ active: currentZoneId === z.id }" @tap="switchZone(z.id)">{{ z.zoneName }}</text>
			</view>

			<!-- 车位网格 -->
			<view class="space-grid" v-if="spaces.length">
				<view class="space-item" v-for="s in spaces" :key="s.id" :class="spaceClass(s)" @tap="selectSpace(s)">
					<text class="space-num">{{ s.spaceNumber }}</text>
				</view>
			</view>
			<view class="no-space" v-else-if="zones.length">
				<text>该分区暂无车位</text>
			</view>
			<view class="loading-space" v-else>
				<text>加载中...</text>
			</view>

			<!-- 图例 -->
			<view class="legend">
				<view class="legend-item"><view class="legend-dot available"></view><text>空闲</text></view>
				<view class="legend-item"><view class="legend-dot occupied"></view><text>占用</text></view>
				<view class="legend-item"><view class="legend-dot selected"></view><text>已选</text></view>
			</view>
		</view>

		<!-- 选中车位 & 预约表单 -->
		<view class="section" v-if="selectedSpace">
			<text class="section-title">预约信息</text>
			<view class="selected-space">
				<text>已选车位：<text class="space-highlight">{{ selectedSpace.spaceNumber }}</text></text>
			</view>

			<view class="form-group">
				<text class="form-label">选择车辆</text>
				<view class="vehicle-picker" @tap="showVehiclePicker = true">
					<text class="picker-text" v-if="selectedVehicle">{{ selectedVehicle.plateNumber }}</text>
					<text class="picker-placeholder" v-else>请选择车辆</text>
					<text class="picker-arrow">&gt;</text>
				</view>
			</view>

			<view class="form-row">
				<view class="form-group flex-1">
					<text class="form-label">开始时间</text>
					<picker class="form-picker" mode="date" :value="startDate" @change="onStartDateChange">
						<text>{{ startDate }}</text>
					</picker>
				</view>
				<view class="form-group flex-1">
					<text class="form-label">&nbsp;</text>
					<picker class="form-picker" mode="time" :value="startTime" @change="onStartTimeChange">
						<text>{{ startTime }}</text>
					</picker>
				</view>
			</view>

			<view class="form-row">
				<view class="form-group flex-1">
					<text class="form-label">结束时间</text>
					<picker class="form-picker" mode="date" :value="endDate" @change="onEndDateChange">
						<text>{{ endDate }}</text>
					</picker>
				</view>
				<view class="form-group flex-1">
					<text class="form-label">&nbsp;</text>
					<picker class="form-picker" mode="time" :value="endTime" @change="onEndTimeChange">
						<text>{{ endTime }}</text>
					</picker>
				</view>
			</view>

			<view class="submit-btn" @tap="createReservation">确认预约</view>
		</view>

		<!-- 车辆选择弹窗 -->
		<view class="modal-overlay" v-if="showVehiclePicker" @tap="showVehiclePicker = false">
			<view class="modal-content" @tap.stop>
				<text class="modal-title">选择车辆</text>
				<view class="vehicle-list">
					<view class="vehicle-option" v-for="v in vehicles" :key="v.id" @tap="pickVehicle(v)">
						<text class="v-plate">{{ v.plateNumber }}</text>
						<text class="v-type">{{ vehicleTypeText(v.vehicleType) }}</text>
						<text class="v-check" v-if="selectedVehicle && selectedVehicle.id === v.id">✓</text>
					</view>
				</view>
				<view class="no-vehicle" v-if="!vehicles.length">
					<text>暂无车辆，请先添加</text>
				</view>
				<view class="modal-close" @tap="showVehiclePicker = false">关闭</view>
			</view>
		</view>
	</view>
</template>

<script>
	import request from '../../common/request'

	export default {
		data() {
			const now = new Date()
			const pad = n => String(n).padStart(2, '0')
			const later = new Date(now.getTime() + 2 * 3600000)
			return {
				park: {},
				stats: {},
				zones: [],
				currentZoneId: null,
				spaces: [],
				selectedSpace: null,

				vehicles: [],
				selectedVehicle: null,
				showVehiclePicker: false,

				startDate: `${now.getFullYear()}-${pad(now.getMonth()+1)}-${pad(now.getDate())}`,
				startTime: `${pad(now.getHours())}:${pad(now.getMinutes())}`,
				endDate: `${later.getFullYear()}-${pad(later.getMonth()+1)}-${pad(later.getDate())}`,
				endTime: `${pad(later.getHours())}:${pad(later.getMinutes())}`
			}
		},
		onLoad(options) {
			const parkId = options.parkId
			if (!parkId) return uni.showToast({ title: '缺少园区ID', icon: 'none' }) && uni.navigateBack()
			this.parkId = parseInt(parkId)
			this.loadParkInfo()
			this.loadZones()
			this.loadVehicles()
		},
		methods: {
			vehicleTypeText(t) {
				return { 1: '小车', 2: '大车', 3: '新能源车' }[t] || '未知'
			},

			getUserId() {
				try {
					const info = JSON.parse(uni.getStorageSync('park_user_info') || '{}')
					return info.id
				} catch { return null }
			},

			loadParkInfo() {
				request.get('/app/park-area/' + this.parkId).then((res) => {
					this.park = res || {}
				}).catch(() => {})

				request.get('/app/park-area/' + this.parkId + '/occupancy-stats').then((res) => {
					this.stats = res || {}
				}).catch(() => {
					this.stats = { availableSpaces: 0, occupiedSpaces: 0 }
				})
			},

			loadZones() {
				request.get('/app/park-area/' + this.parkId + '/zones').then((zones) => {
					this.zones = Array.isArray(zones) ? zones : (zones || [])
					if (this.zones.length) {
						this.currentZoneId = this.zones[0].id
						this.loadSpaces()
					}
				}).catch(() => {
					this.zones = []
				})
			},

			switchZone(zoneId) {
				this.currentZoneId = zoneId
				this.selectedSpace = null
				this.loadSpaces()
			},

			loadSpaces() {
				if (!this.currentZoneId) return
				this.spaces = []
				request.get('/app/parking-space/available?parkAreaId=' + this.parkId).then((spaces) => {
					this.spaces = Array.isArray(spaces) ? spaces : (spaces || [])
				}).catch(() => {
					this.spaces = []
				})
			},

			spaceClass(s) {
				if (this.selectedSpace && this.selectedSpace.id === s.id) return 'space-selected'
				if (s.currentOccupiedStatus === 1) return 'space-occupied'
				if (s.status === 0 || s.status === 4) return 'space-disabled'
				return 'space-available'
			},

			selectSpace(s) {
				if (s.currentOccupiedStatus === 1) return uni.showToast({ title: '该车位已被占用', icon: 'none' })
				if (s.status === 0 || s.status === 4) return uni.showToast({ title: '该车位不可用', icon: 'none' })
				this.selectedSpace = this.selectedSpace && this.selectedSpace.id === s.id ? null : s
			},

			loadVehicles() {
				request.get('/app/vehicle/my').then((vehicles) => {
					const list = Array.isArray(vehicles) ? vehicles : (vehicles || [])
					this.vehicles = list
					const def = list.find(v => v.isDefault === 1)
					if (def) this.selectedVehicle = def
				}).catch(() => {})
			},

			pickVehicle(v) {
				this.selectedVehicle = v
				this.showVehiclePicker = false
			},

			onStartDateChange(e) { this.startDate = e.detail.value },
			onStartTimeChange(e) { this.startTime = e.detail.value },
			onEndDateChange(e) { this.endDate = e.detail.value },
			onEndTimeChange(e) { this.endTime = e.detail.value },

			createReservation() {
					if (!this.selectedSpace) return uni.showToast({ title: '请选择车位', icon: 'none' })
					if (!this.selectedVehicle) return uni.showToast({ title: '请选择车辆', icon: 'none' })

					const start = new Date(this.startDate + ' ' + this.startTime)
					const end = new Date(this.endDate + ' ' + this.endTime)

					if (start >= end) return uni.showToast({ title: '结束时间须晚于开始时间', icon: 'none' })

					uni.showLoading({ title: '提交中...', mask: true })

					request.post('/app/reservation', {
						vehicleId: this.selectedVehicle.id,
						spaceId: this.selectedSpace.id,
						startTime: this.startDate + ' ' + this.startTime + ':00',
						endTime: this.endDate + ' ' + this.endTime + ':00'
					}).then((res) => {
						uni.hideLoading()
						uni.showToast({ title: '预约成功', icon: 'success' })
						setTimeout(() => uni.navigateBack(), 1000)
					}).catch((err) => {
						uni.hideLoading()
						const msg = err.code === 409 ? '该预约已被其他用户抢先，请重新选择' : (err.message || '预约失败')
						uni.showToast({ title: msg, icon: 'none' })
					})
				}
		}
	}
</script>

<style lang="scss">
.page-detail {
	min-height: 100vh;
	background: #f5f5f5;
	padding-bottom: 40rpx;
}

.park-header {
	background: linear-gradient(135deg, #3B86FF, #6AA5FF);
	padding: 40rpx 30rpx;
	color: #fff;
}
.park-name {
	font-size: 36rpx;
	font-weight: bold;
}
.park-addr {
	font-size: 24rpx;
	color: rgba(255,255,255,0.8);
	margin-top: 8rpx;
	display: block;
}
.park-stats {
	display: flex;
	margin-top: 24rpx;
	.stat {
		flex: 1;
		text-align: center;
		.stat-num { font-size: 40rpx; font-weight: bold; color: #fff; &.green { color: #81C784; } &.red { color: #EF9A9A; } }
		.stat-label { font-size: 22rpx; color: rgba(255,255,255,0.7); display: block; margin-top: 4rpx; }
	}
}

.section {
	background: #fff;
	margin: 20rpx 30rpx;
	border-radius: 16rpx;
	padding: 30rpx;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}
.section-title {
	font-size: 30rpx;
	font-weight: bold;
	color: #333;
	display: block;
	margin-bottom: 20rpx;
}

.zone-tabs {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	margin-bottom: 20rpx;
}
.zone-tab {
	padding: 10rpx 28rpx;
	border: 1rpx solid #e0e0e0;
	border-radius: 30rpx;
	font-size: 24rpx;
	color: #666;
	&.active {
		border-color: #3B86FF;
		color: #3B86FF;
		background: #F0F4FF;
	}
}

.space-grid {
	display: grid;
	grid-template-columns: repeat(5, 1fr);
	gap: 16rpx;
}
.space-item {
	aspect-ratio: 1;
	border-radius: 12rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	.space-num { font-size: 22rpx; font-weight: 500; }
	&.space-available { background: #E8F5E9; .space-num { color: #4CAF50; } }
	&.space-occupied { background: #FFEBEE; .space-num { color: #F44336; } }
	&.space-disabled { background: #f5f5f5; .space-num { color: #ccc; } }
	&.space-selected { background: #3B86FF; .space-num { color: #fff; } }
}

.no-space, .loading-space {
	padding: 40rpx 0;
	text-align: center;
	color: #999;
	font-size: 26rpx;
}

.legend {
	display: flex;
	gap: 24rpx;
	margin-top: 20rpx;
}
.legend-item {
	display: flex;
	align-items: center;
	gap: 8rpx;
	font-size: 22rpx;
	color: #999;
}
.legend-dot {
	width: 20rpx;
	height: 20rpx;
	border-radius: 4rpx;
	&.available { background: #E8F5E9; }
	&.occupied { background: #FFEBEE; }
	&.selected { background: #3B86FF; }
}

.selected-space {
	background: #F0F4FF;
	padding: 16rpx 20rpx;
	border-radius: 8rpx;
	font-size: 26rpx;
	color: #333;
	margin-bottom: 24rpx;
	.space-highlight { color: #3B86FF; font-weight: bold; font-size: 28rpx; }
}

.form-group {
	margin-bottom: 24rpx;
	&.flex-1 { flex: 1; }
}
.form-label {
	font-size: 26rpx;
	color: #666;
	display: block;
	margin-bottom: 12rpx;
}
.form-row {
	display: flex;
	gap: 20rpx;
}
.form-picker {
	height: 72rpx;
	border: 1rpx solid #e0e0e0;
	border-radius: 8rpx;
	padding: 0 20rpx;
	font-size: 26rpx;
	color: #333;
	display: flex;
	align-items: center;
}
.vehicle-picker {
	height: 72rpx;
	border: 1rpx solid #e0e0e0;
	border-radius: 8rpx;
	padding: 0 20rpx;
	display: flex;
	align-items: center;
	.picker-text { flex: 1; font-size: 26rpx; color: #333; }
	.picker-placeholder { flex: 1; font-size: 26rpx; color: #ccc; }
	.picker-arrow { font-size: 24rpx; color: #ccc; }
}

.submit-btn {
	height: 88rpx;
	background: #3B86FF;
	color: #fff;
	border-radius: 44rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 30rpx;
	margin-top: 32rpx;
	box-shadow: 0 4rpx 20rpx rgba(59,134,255,0.3);
}

/* 弹窗 */
.modal-overlay {
	position: fixed;
	top: 0; left: 0; right: 0; bottom: 0;
	background: rgba(0,0,0,0.5);
	z-index: 100;
	display: flex;
	align-items: center;
	justify-content: center;
}
.modal-content {
	background: #fff;
	border-radius: 20rpx;
	padding: 40rpx;
	width: 600rpx;
	max-height: 70vh;
	overflow-y: auto;
}
.modal-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	display: block;
	text-align: center;
	margin-bottom: 24rpx;
}
.vehicle-option {
	display: flex;
	align-items: center;
	padding: 24rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
	.v-plate { flex: 1; font-size: 28rpx; color: #333; font-weight: 500; }
	.v-type { font-size: 24rpx; color: #999; margin-right: 16rpx; }
	.v-check { font-size: 28rpx; color: #3B86FF; font-weight: bold; }
}
.no-vehicle {
	padding: 40rpx 0;
	text-align: center;
	color: #999;
	font-size: 26rpx;
}
.modal-close {
	text-align: center;
	padding: 20rpx 0 0;
	color: #999;
	font-size: 26rpx;
}
</style>
