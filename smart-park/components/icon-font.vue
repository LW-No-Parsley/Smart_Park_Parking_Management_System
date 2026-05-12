<template>
	<text class="iconfont" :class="'icon-' + name" :style="iconStyle"></text>
</template>

<script>
	export default {
		props: {
			name: { type: String, required: true },
			size: { type: String, default: '32rpx' },
			color: { type: String, default: '' }
		},
		computed: {
			iconStyle() {
				const style = {}
				if (this.size) style.fontSize = this.size
				if (this.color) style.color = this.color
				return style
			}
		}
	}
</script>

<style>
	.iconfont {
		font-family: "iconfont" !important;
		font-style: normal;
		-webkit-font-smoothing: antialiased;
		-moz-osx-font-smoothing: grayscale;
	}
</style>
