<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
		/* iconfont */
		@import "./static/iconfont/iconfont.css";

	/* 每个页面公共css */
	page {
		background-color: #F5F5F5;
		font-family: -apple-system, BlinkMacSystemFont, 'Helvetica Neue', Helvetica, Arial, sans-serif;
		box-sizing: border-box;
	}
	view, scroll-view, swiper {
		box-sizing: border-box;
	}
</style>
