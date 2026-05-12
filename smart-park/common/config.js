/**
 * 应用全局配置
 * 使用前请在高德开放平台 (https://lbs.amap.com) 申请 Key
 */
export default {
	// 后端 API 基础地址
	baseUrl: 'http://localhost:8080/api',

	// 高德地图 Key
	amapKey: 'YOUR_AMAP_KEY_HERE',

	// 各平台 Key（manifest.json 中也需要配置）
	amapKeys: {
		android: 'YOUR_ANDROID_KEY',
		ios: 'YOUR_IOS_KEY',
		web: 'YOUR_WEB_KEY'
	},

	// 默认定位（上海市中心）
	defaultLocation: {
		latitude: 31.2304,
		longitude: 121.4737
	}
}
