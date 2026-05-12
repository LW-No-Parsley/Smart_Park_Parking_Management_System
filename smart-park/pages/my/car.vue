<template>
	<view class="page-car">
		<!-- 车辆列表 -->
		<view class="car-list" v-if="carList.length">
			<view class="car-item" v-for="item in carList" :key="item.id">
				<view class="car-header">
					<view class="car-plate">
						<text class="plate-text">{{ item.plateNumber }}</text>
						<text class="default-tag" v-if="item.isDefault === 1">默认</text>
					</view>
					<text class="delete-btn" @tap="deleteCar(item)">删除</text>
				</view>
				<view class="car-body">
					<text class="car-info">类型：{{ vehicleTypeText(item.vehicleType) }}</text>
					<text class="car-info" v-if="item.brand">品牌：{{ item.brand }}</text>
					<text class="car-info" v-if="item.color">颜色：{{ item.color }}</text>
				</view>
			</view>
		</view>

		<!-- 空状态 -->
		<view class="empty-state" v-else>
			<icon-font class="empty-icon" name="car" size="80rpx" color="#ddd"></icon-font>
			<text class="empty-text">暂无车辆，点击下方添加</text>
		</view>

		<!-- 添加按钮 -->
		<view class="add-btn" @tap="showAddForm">
			<text class="add-icon">+</text>
			<text>添加车辆</text>
		</view>

		<!-- 添加车辆弹窗 -->
		<view class="modal-overlay" v-if="showForm" @tap="closeForm">
			<view class="modal-content" @tap.stop>
				<text class="modal-title">添加车辆</text>

				<view class="form-group">
					<text class="form-label">车牌号 *</text>
					<input class="form-input" v-model="formData.plateNumber" placeholder="如：京A12345" maxlength="10" />
				</view>

				<view class="form-group">
					<text class="form-label">车辆类型</text>
					<view class="form-radio-group">
						<text class="form-radio" :class="{ active: formData.vehicleType === 1 }" @tap="formData.vehicleType = 1">小车</text>
						<text class="form-radio" :class="{ active: formData.vehicleType === 2 }" @tap="formData.vehicleType = 2">大车</text>
						<text class="form-radio" :class="{ active: formData.vehicleType === 3 }" @tap="formData.vehicleType = 3">新能源</text>
					</view>
				</view>

				<view class="form-group">
					<text class="form-label">品牌</text>
					<input class="form-input" v-model="formData.brand" placeholder="如：特斯拉" maxlength="20" />
				</view>

				<view class="form-group">
					<text class="form-label">颜色</text>
					<input class="form-input" v-model="formData.color" placeholder="如：白色" maxlength="10" />
				</view>

				<view class="form-buttons">
					<view class="btn-cancel" @tap="closeForm">取消</view>
					<view class="btn-submit" @tap="submitForm">添加</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import request from '../../common/request'

	const VEHICLE_TYPE_MAP = { 1: '小车', 2: '大车', 3: '新能源车' }

	export default {
		data() {
			return {
				carList: [],
				showForm: false,
				formData: {
					plateNumber: '',
					vehicleType: 1,
					brand: '',
					color: ''
				}
			}
		},
		onShow() {
			this.loadCarList()
		},
		methods: {
			vehicleTypeText(type) {
				return VEHICLE_TYPE_MAP[type] || '未知'
			},

			loadCarList() {
				request.get('/app/vehicle/my').then((res) => {
					this.carList = Array.isArray(res) ? res : (res || [])
				}).catch(() => {
					this.carList = []
				})
			},

			showAddForm() {
				this.formData = { plateNumber: '', vehicleType: 1, brand: '', color: '' }
				this.showForm = true
			},

			closeForm() {
				this.showForm = false
			},

			submitForm() {
				if (!this.formData.plateNumber.trim()) {
					return uni.showToast({ title: '请输入车牌号', icon: 'none' })
				}
				uni.showLoading({ title: '添加中...', mask: true })
				request.post('/app/vehicle', this.formData).then(() => {
					uni.hideLoading()
					uni.showToast({ title: '添加成功', icon: 'success' })
					this.showForm = false
					this.loadCarList()
				}).catch((err) => {
					uni.hideLoading()
					uni.showToast({ title: err.message || '添加失败', icon: 'none' })
				})
			},

			deleteCar(item) {
				uni.showModal({
					title: '提示',
					content: `确定要删除车牌 ${item.plateNumber} 吗？`,
					success: (res) => {
						if (res.confirm) {
							uni.showLoading({ title: '删除中...', mask: true })
							request.delete(`/app/vehicle/${item.id}`).then(() => {
								uni.hideLoading()
								uni.showToast({ title: '删除成功', icon: 'success' })
								this.loadCarList()
							}).catch((err) => {
								uni.hideLoading()
								uni.showToast({ title: err.message || '删除失败', icon: 'none' })
							})
						}
					}
				})
			}
		}
	}
</script>

<style lang="scss">
.page-car {
	min-height: 100vh;
	background: #f5f5f5;
	padding: 20rpx 30rpx 140rpx;
}

.car-item {
	background: #fff;
	border-radius: 16rpx;
	padding: 30rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.06);
}

.car-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 16rpx;
}

.car-plate {
	display: flex;
	align-items: center;
	gap: 12rpx;
	.plate-text {
		font-size: 34rpx;
		font-weight: bold;
		color: #333;
		letter-spacing: 2rpx;
	}
}

.default-tag {
	font-size: 20rpx;
	color: #3B86FF;
	background: #F0F4FF;
	padding: 2rpx 14rpx;
	border-radius: 4rpx;
}

.delete-btn {
	font-size: 24rpx;
	color: #F44336;
	padding: 6rpx 16rpx;
	border: 1rpx solid #F44336;
	border-radius: 6rpx;
}

.car-body {
	display: flex;
	flex-wrap: wrap;
	gap: 12rpx;
	.car-info {
		font-size: 24rpx;
		color: #666;
		background: #f8f8f8;
		padding: 4rpx 14rpx;
		border-radius: 4rpx;
	}
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	padding: 120rpx 0;
	.empty-icon { font-size: 80rpx; }
	.empty-text { font-size: 26rpx; color: #999; margin-top: 20rpx; }
}

.add-btn {
	position: fixed;
	bottom: 40rpx;
	left: 30rpx;
	right: 30rpx;
	height: 88rpx;
	background: #3B86FF;
	color: #fff;
	border-radius: 44rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 30rpx;
	box-shadow: 0 4rpx 20rpx rgba(59,134,255,0.3);
	.add-icon {
		font-size: 36rpx;
		margin-right: 8rpx;
	}
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
	width: 640rpx;
	max-height: 80vh;
	overflow-y: auto;
}
.modal-title {
	font-size: 34rpx;
	font-weight: bold;
	color: #333;
	margin-bottom: 32rpx;
	display: block;
	text-align: center;
}
.form-group {
	margin-bottom: 24rpx;
}
.form-label {
	font-size: 26rpx;
	color: #666;
	display: block;
	margin-bottom: 12rpx;
}
.form-input {
	width: 100%;
	height: 72rpx;
	border: 1rpx solid #e0e0e0;
	border-radius: 8rpx;
	padding: 0 20rpx;
	font-size: 26rpx;
	box-sizing: border-box;
}
.form-radio-group {
	display: flex;
	gap: 16rpx;
}
.form-radio {
	padding: 12rpx 28rpx;
	border: 1rpx solid #e0e0e0;
	border-radius: 8rpx;
	font-size: 26rpx;
	color: #666;
	&.active {
		border-color: #3B86FF;
		color: #3B86FF;
		background: #F0F4FF;
	}
}
.form-buttons {
	display: flex;
	gap: 20rpx;
	margin-top: 32rpx;
	.btn-cancel, .btn-submit {
		flex: 1;
		height: 80rpx;
		border-radius: 40rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
	}
	.btn-cancel {
		background: #f5f5f5;
		color: #666;
	}
	.btn-submit {
		background: #3B86FF;
		color: #fff;
	}
}
</style>
