<template>
	<view class="container">
		<view class="header">
			<text class="title">下載文件</text>
			<text class="subtitle">安全、快速的文件下載服務</text>
		</view>
		
		<view class="download-section">
			<!-- 輸入取件碼 -->
			<view v-if="!fileInfo" class="input-section">
				<view class="input-box">
					<text class="input-label">請輸入取件碼</text>
					<input 
						class="input-field" 
						v-model="pickupCode" 
						placeholder="輸入6位取件碼"
						maxlength="6"
						@input="onInputChange"
					/>
				</view>
				
				<!-- 驗證碼區域 -->
				<view v-if="showCaptcha" class="captcha-section">
					<view class="captcha-label">安全驗證</view>
					<view class="captcha-box">
						<input 
							class="captcha-input" 
							v-model="captchaCode" 
							placeholder="輸入驗證碼"
							maxlength="4"
						/>
						<view class="captcha-display" @click="refreshCaptcha">
							<text class="captcha-text">{{ captchaDisplay }}</text>
							<text class="refresh-hint">點擊刷新</text>
						</view>
					</view>
				</view>
				
				<button 
					class="query-btn" 
					@click="queryFile" 
					:disabled="!canQuery || loading"
				>
					{{ loading ? '查詢中...' : '查詢文件' }}
				</button>
				
				<!-- 安全提示 -->
				<view class="security-tip">
					<text class="tip-icon">🔒</text>
					<text class="tip-text">為保護您的文件安全，系統會記錄訪問行為並限制異常訪問</text>
				</view>
			</view>
			
			<!-- 文件信息顯示 -->
			<view v-if="fileInfo && !downloading" class="file-info-section">
				<view class="file-card">
					<view class="file-icon">📄</view>
					<view class="file-details">
						<text class="file-name">{{ fileInfo.fileName }}</text>
						<text class="file-size">文件大小: {{ formatFileSize(fileInfo.fileSize) }}</text>
						<text class="file-time" v-if="fileInfo.uploadTime">上傳時間: {{ fileInfo.uploadTime }}</text>
					</view>
				</view>
				<button class="download-btn" @click="startDownload">下載文件</button>
			</view>
			
			<!-- 下載進度 -->
			<view v-if="downloading" class="download-progress">
				<view class="progress-info">
					<text class="progress-text">下載中...</text>
					<text class="progress-percent">{{ downloadProgress }}%</text>
				</view>
				<progress :percent="downloadProgress" stroke-width="8" color="#667eea" />
				<text class="progress-detail">已下載: {{ formatFileSize(downloadedSize) }} / {{ formatFileSize(totalSize) }}</text>
			</view>
			
			<!-- 下載完成 -->
			<view v-if="downloadComplete" class="download-complete">
				<view class="success-icon">✅</view>
				<text class="success-title">下載完成！</text>
				<button class="back-btn" @click="goBack">返回首頁</button>
			</view>
		</view>
	</view>
</template>

<script>
	import { getFileInfo, getDownloadUrl, getCaptcha, getFileInfoWithCaptcha } from '@/utils/api.js'
	import { formatFileSize } from '@/utils/fileUtil.js'
	
	export default {
		data() {
			return {
				pickupCode: '',
				fileInfo: null,
				downloading: false,
				downloadComplete: false,
				downloadProgress: 0,
				downloadedSize: 0,
				totalSize: 0,
				loading: false,
				showCaptcha: false,
				captchaId: '',
				captchaCode: '',
				captchaDisplay: ''
			}
		},
		computed: {
			canQuery() {
				return this.pickupCode.length === 6
			}
		},
		methods: {
			// 輸入變化
			onInputChange(e) {
				this.pickupCode = e.detail.value.toUpperCase()
				// 當輸入6位取件碼時，顯示驗證碼
				if (this.pickupCode.length === 6 && !this.showCaptcha) {
					this.loadCaptcha()
				}
			},
			
			// 加載驗證碼
			async loadCaptcha() {
				try {
					const result = await getCaptcha()
					this.captchaId = result.captchaId
					this.captchaDisplay = result.captchaCode
					this.showCaptcha = true
				} catch (error) {
					console.error('加載驗證碼失敗:', error)
				}
			},
			
			// 刷新驗證碼
			refreshCaptcha() {
				this.captchaCode = ''
				this.loadCaptcha()
			},
			
			// 查詢文件信息
			async queryFile() {
				if (!this.canQuery) {
					uni.showToast({
						title: '請輸入6位取件碼',
						icon: 'none'
					})
					return
				}
				
				if (this.showCaptcha && !this.captchaCode) {
					uni.showToast({
						title: '請輸入驗證碼',
						icon: 'none'
					})
					return
				}
				
				this.loading = true
				uni.showLoading({
					title: '查詢中...'
				})
				
				try {
					let result
					if (this.showCaptcha) {
						result = await getFileInfoWithCaptcha(this.pickupCode, this.captchaId, this.captchaCode)
					} else {
						result = await getFileInfo(this.pickupCode)
					}
					
					this.fileInfo = result.data || result
					this.totalSize = this.fileInfo.fileSize
					
					uni.hideLoading()
					uni.showToast({
						title: '查詢成功',
						icon: 'success'
					})
				} catch (error) {
					console.error('查詢失敗:', error)
					uni.hideLoading()
					
					// 如果是驗證碼錯誤，刷新驗證碼
					if (error.message && error.message.includes('驗證碼')) {
						this.refreshCaptcha()
					}
					
					uni.showToast({
						title: '查詢失敗: ' + (error.message || '取件碼不存在'),
						icon: 'none',
						duration: 3000
					})
				} finally {
					this.loading = false
				}
			},
			
			// 開始下載
			async startDownload() {
				if (!this.fileInfo) {
					return
				}
				
				this.downloading = true
				this.downloadProgress = 0
				this.downloadedSize = 0
				
				try {
					const downloadUrl = getDownloadUrl(this.pickupCode)
					
					// #ifdef H5
					const response = await fetch(downloadUrl)
					const reader = response.body.getReader()
					const contentLength = parseInt(response.headers.get('Content-Length') || '0')
					
					const chunks = []
					let receivedLength = 0
					
					while (true) {
						const { done, value } = await reader.read()
						if (done) break
						
						chunks.push(value)
						receivedLength += value.length
						this.downloadedSize = receivedLength
						this.downloadProgress = contentLength > 0 
							? Math.round((receivedLength / contentLength) * 100) 
							: 0
					}
					
					const allChunks = new Uint8Array(receivedLength)
					let position = 0
					for (const chunk of chunks) {
						allChunks.set(chunk, position)
						position += chunk.length
					}
					
					const blob = new Blob([allChunks])
					const url = window.URL.createObjectURL(blob)
					const a = document.createElement('a')
					a.href = url
					a.download = this.fileInfo.fileName
					document.body.appendChild(a)
					a.click()
					document.body.removeChild(a)
					window.URL.revokeObjectURL(url)
					// #endif
					
					// #ifdef MP-WEIXIN
					uni.downloadFile({
						url: downloadUrl,
						success: (res) => {
							if (res.statusCode === 200) {
								uni.saveFile({
									tempFilePath: res.tempFilePath,
									success: (saveRes) => {
										uni.showToast({
											title: '文件已保存',
											icon: 'success'
										})
										this.downloading = false
										this.downloadComplete = true
									}
								})
							}
						}
					})
					// #endif
					
					this.downloading = false
					this.downloadComplete = true
					
					uni.showToast({
						title: '下載完成',
						icon: 'success'
					})
				} catch (error) {
					console.error('下載失敗:', error)
					this.downloading = false
					uni.showToast({
						title: '下載失敗: ' + (error.message || '未知錯誤'),
						icon: 'none',
						duration: 3000
					})
				}
			},
			
			// 返回首頁
			goBack() {
				uni.reLaunch({
					url: '/pages/index/index'
				})
			},
			
			// 格式化文件大小
			formatFileSize
		}
	}
</script>

<style scoped>
	.container {
		min-height: 100vh;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		padding: 40rpx;
		box-sizing: border-box;
	}
	
	.header {
		text-align: center;
		margin-bottom: 60rpx;
		padding-top: 40rpx;
	}
	
	.title {
		display: block;
		font-size: 56rpx;
		font-weight: bold;
		color: #ffffff;
		margin-bottom: 20rpx;
	}
	
	.subtitle {
		display: block;
		font-size: 28rpx;
		color: rgba(255, 255, 255, 0.9);
	}
	
	.download-section {
		background: #ffffff;
		border-radius: 32rpx;
		padding: 60rpx 40rpx;
		box-shadow: 0 20rpx 60rpx rgba(0, 0, 0, 0.15);
		max-width: 800rpx;
		margin: 0 auto;
	}
	
	.input-section {
		display: flex;
		flex-direction: column;
		gap: 40rpx;
	}
	
	.input-box {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
	
	.input-label {
		font-size: 30rpx;
		color: #333333;
		font-weight: 500;
	}
	
	.input-field {
		width: 100%;
		height: 100rpx;
		padding: 0 30rpx;
		background: #f8f9fa;
		border-radius: 16rpx;
		font-size: 40rpx;
		text-align: center;
		letter-spacing: 12rpx;
		font-weight: bold;
		color: #333333;
		border: 2rpx solid transparent;
		transition: all 0.3s;
	}
	
	.input-field:focus {
		border-color: #667eea;
		background: #ffffff;
		box-shadow: 0 0 0 4rpx rgba(102, 126, 234, 0.1);
	}
	
	.captcha-section {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
		padding: 30rpx;
		background: #f8f9fa;
		border-radius: 16rpx;
	}
	
	.captcha-label {
		font-size: 28rpx;
		color: #666666;
	}
	
	.captcha-box {
		display: flex;
		gap: 20rpx;
		align-items: center;
	}
	
	.captcha-input {
		flex: 1;
		height: 80rpx;
		padding: 0 20rpx;
		background: #ffffff;
		border-radius: 12rpx;
		font-size: 32rpx;
		text-align: center;
		letter-spacing: 8rpx;
	}
	
	.captcha-display {
		width: 200rpx;
		height: 80rpx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		border-radius: 12rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		cursor: pointer;
	}
	
	.captcha-text {
		font-size: 36rpx;
		font-weight: bold;
		color: #ffffff;
		letter-spacing: 4rpx;
	}
	
	.refresh-hint {
		font-size: 20rpx;
		color: rgba(255, 255, 255, 0.8);
		margin-top: 4rpx;
	}
	
	.query-btn {
		width: 100%;
		height: 96rpx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #ffffff;
		border: none;
		border-radius: 16rpx;
		font-size: 34rpx;
		font-weight: bold;
		box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
		transition: all 0.3s;
	}
	
	.query-btn:active {
		transform: scale(0.98);
		box-shadow: 0 4rpx 12rpx rgba(102, 126, 234, 0.3);
	}
	
	.query-btn[disabled] {
		opacity: 0.6;
	}
	
	.security-tip {
		display: flex;
		align-items: flex-start;
		gap: 12rpx;
		padding: 24rpx;
		background: #fff3cd;
		border-radius: 12rpx;
		border-left: 4rpx solid #ffc107;
	}
	
	.tip-icon {
		font-size: 32rpx;
	}
	
	.tip-text {
		flex: 1;
		font-size: 24rpx;
		color: #856404;
		line-height: 1.6;
	}
	
	.file-info-section {
		display: flex;
		flex-direction: column;
		gap: 40rpx;
	}
	
	.file-card {
		display: flex;
		align-items: center;
		gap: 30rpx;
		padding: 40rpx;
		background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
		border-radius: 20rpx;
		box-shadow: 0 4rpx 12rpx rgba(0, 0, 0, 0.05);
	}
	
	.file-icon {
		font-size: 80rpx;
	}
	
	.file-details {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}
	
	.file-name {
		font-size: 32rpx;
		color: #333333;
		font-weight: bold;
		word-break: break-all;
	}
	
	.file-size {
		font-size: 26rpx;
		color: #666666;
	}
	
	.file-time {
		font-size: 24rpx;
		color: #999999;
	}
	
	.download-btn {
		width: 100%;
		height: 96rpx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #ffffff;
		border: none;
		border-radius: 16rpx;
		font-size: 34rpx;
		font-weight: bold;
		box-shadow: 0 8rpx 24rpx rgba(102, 126, 234, 0.3);
	}
	
	.download-progress {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}
	
	.progress-info {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}
	
	.progress-text {
		font-size: 30rpx;
		color: #333333;
	}
	
	.progress-percent {
		font-size: 30rpx;
		color: #667eea;
		font-weight: bold;
	}
	
	.progress-detail {
		font-size: 24rpx;
		color: #999999;
		text-align: center;
	}
	
	.download-complete {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 40rpx;
	}
	
	.success-icon {
		font-size: 120rpx;
	}
	
	.success-title {
		font-size: 40rpx;
		font-weight: bold;
		color: #333333;
	}
	
	.back-btn {
		width: 100%;
		height: 88rpx;
		background: #f8f9fa;
		color: #333333;
		border: none;
		border-radius: 16rpx;
		font-size: 32rpx;
	}
	
	/* 響應式設計 */
	@media screen and (min-width: 768px) {
		.download-section {
			padding: 80rpx 60rpx;
		}
	}
</style>

