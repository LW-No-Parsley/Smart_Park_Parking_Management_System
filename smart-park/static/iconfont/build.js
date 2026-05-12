/**
 * iconfont 构建脚本
 * 使用方法：
 *   1. 从 iconfont.cn 下载 Font class 包，解压
 *   2. 将 iconfont.css 和 iconfont.ttf 放到 static/iconfont/ 目录
 *   3. 运行: node static/iconfont/build.js
 *   4. 生成的 iconfont.css 已内嵌 base64 字体，可直接用于小程序
 */

const fs = require('fs')
const path = require('path')

const DIR = path.join(__dirname)
const TTF_FILE = path.join(DIR, 'iconfont.ttf')
const CSS_FILE = path.join(DIR, 'iconfont.css')
const OUTPUT = path.join(DIR, 'iconfont.css')

function build() {
	// 读取 TTF 并转 base64
	if (!fs.existsSync(TTF_FILE)) {
		console.error('❌ 未找到 iconfont.ttf，请先从 iconfont.cn 下载')
		process.exit(1)
	}
	const ttfBuffer = fs.readFileSync(TTF_FILE)
	const base64Font = ttfBuffer.toString('base64')

	// 读取原始 CSS
	let css = fs.readFileSync(CSS_FILE, 'utf8')

	// 替换 @font-face 中的 src
	const fontFaceRegex = /@font-face\s*\{[^}]+\}/
	const newFontFace = `@font-face {
  font-family: "iconfont";
  src: url('data:application/octet-stream;base64,${base64Font}') format('truetype');
}`

	if (fontFaceRegex.test(css)) {
		css = css.replace(fontFaceRegex, newFontFace)
	} else {
		// 没有 @font-face，在前面插入
		css = newFontFace + '\n\n' + css
	}

	// 移除旧 CSS 顶部的注释占位
	const headerEnd = css.indexOf('*/')
	const afterHeader = css.substring(headerEnd + 2)
	const contentStart = afterHeader.search(/\S/)
	const cleanContent = contentStart > 0 ? afterHeader.substring(contentStart) : afterHeader

	const finalCss = `/*
 * iconfont 图标库（内嵌 base64 字体，兼容小程序）
 * 生成时间: ${new Date().toISOString()}
 * 由 build.js 自动构建，请勿手动修改
 */

${newFontFace}

.iconfont {
  font-family: "iconfont" !important;
  font-style: normal;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

${cleanContent}
`

	fs.writeFileSync(OUTPUT, finalCss, 'utf8')
	console.log('✅ iconfont.css 构建完成！')
}

build()
