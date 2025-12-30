<template>
	<view class="container">
		<view class="header">
			<text class="title">上传文件</text>
		</view>
		
		<view class="upload-section">
			<!-- 文件选择区域 -->
			<view v-if="!selectedFile" class="upload-area" @click="selectFile">
				<view class="upload-icon">📁</view>
				<text class="upload-text">点击选择文件</text>
				<text class="upload-hint">支持大文件上传（最大支持 10GB）</text>
			</view>
			
			<!-- 文件信息显示 -->
			<view v-if="selectedFile && !uploading && !uploadComplete" class="file-info">
				<view class="file-item">
					<text class="file-name">{{ selectedFile.name }}</text>
					<text class="file-size">{{ formatFileSize(selectedFile.size) }}</text>
				</view>
				<button class="upload-btn" @click="startUpload" :disabled="uploading">开始上传</button>
			</view>
			
			<!-- 上传进度 -->
			<view v-if="uploading" class="upload-progress">
				<view class="progress-info">
					<text class="progress-text">上传中...</text>
					<text class="progress-percent">{{ uploadProgress }}%</text>
				</view>
				<progress :percent="uploadProgress" stroke-width="8" color="#667eea" />
				<text class="progress-detail">已上传: {{ formatFileSize(uploadedSize) }} / {{ formatFileSize(totalSize) }}</text>
			</view>
			
			<!-- 上传完成，显示取件码 -->
			<view v-if="uploadComplete" class="pickup-code-section">
				<view class="success-icon">✅</view>
				<text class="success-title">上传成功！</text>
				<view class="pickup-code-box">
					<text class="pickup-label">取件码</text>
					<text class="pickup-code" @click="copyPickupCode">{{ pickupCode }}</text>
					<text class="pickup-hint">点击复制取件码，分享给需要下载的用户</text>
				</view>
				<button class="back-btn" @click="goBack">返回首页</button>
			</view>
		</view>
	</view>
</template>

<script>
	import { initUpload, uploadChunk, completeUpload } from '@/utils/api.js'
	import { splitFile, formatFileSize, calculateFileId } from '@/utils/fileUtil.js'
	import { BASE_URL } from '@/utils/config.js'
	
	export default {
		data() {
			return {
				selectedFile: null,
				uploading: false,
				uploadComplete: false,
				uploadProgress: 0,
				uploadedSize: 0,
				totalSize: 0,
				pickupCode: '',
				uploadId: '',
				chunks: [],
				currentChunkIndex: 0
			}
		},
		methods: {
			// 选择文件
			selectFile() {
				// #ifdef H5
				// H5 环境
				const input = document.createElement('input')
				input.type = 'file'
				input.onchange = (e) => {
					const file = e.target.files[0]
					if (file) {
						this.selectedFile = file
						this.totalSize = file.size
					}
				}
				input.click()
				// #endif
				
				// #ifdef MP-WEIXIN
				// 微信小程序环境
				uni.chooseFile({
					count: 1,
					success: (res) => {
						const file = res.tempFiles[0]
						this.selectedFile = {
							name: file.name,
							size: file.size,
							path: file.path
						}
						this.totalSize = file.size
					},
					fail: (err) => {
						uni.showToast({
							title: '选择文件失败',
							icon: 'none'
						})
					}
				})
				// #endif
			},
			
			// 开始上传
			async startUpload() {
				if (!this.selectedFile) {
					uni.showToast({
						title: '请先选择文件',
						icon: 'none'
					})
					return
				}
				
				this.uploading = true
				this.uploadProgress = 0
				this.uploadedSize = 0
				
				try {
					// 1. 初始化上传（获取上传ID）
					const fileId = calculateFileId(this.selectedFile)
					const initResult = await initUpload(
						this.selectedFile.name,
						this.selectedFile.size,
						fileId
					)
					
					this.uploadId = initResult.uploadId || initResult.data?.uploadId
					
					// 2. 分片上传
					await this.uploadChunks()
					
					// 3. 完成上传，获取取件码
					const completeResult = await completeUpload(this.uploadId)
					this.pickupCode = completeResult.pickupCode || completeResult.data?.pickupCode
					
					this.uploading = false
					this.uploadComplete = true
					
					uni.showToast({
						title: '上传成功',
						icon: 'success'
					})
				} catch (error) {
					console.error('上传失败:', error)
					this.uploading = false
					uni.showToast({
						title: '上传失败: ' + (error.message || '未知错误'),
						icon: 'none',
						duration: 3000
					})
				}
			},
			
			// 分片上传
			async uploadChunks() {
				// 获取文件分片
				this.chunks = await splitFile(this.selectedFile)
				const totalChunks = this.chunks.length
				
				// 逐个上传分片
				for (let i = 0; i < this.chunks.length; i++) {
					const chunk = this.chunks[i]
					
					try {
						// #ifdef H5
						// H5 环境：使用 FormData 上传
						const formData = new FormData()
						formData.append('chunk', chunk.chunk)
						formData.append('uploadId', this.uploadId)
						formData.append('chunkIndex', chunk.index.toString())
						formData.append('chunkMd5', `chunk_${chunk.index}`)
						
						const response = await fetch(`${BASE_URL}/file/upload`, {
							method: 'POST',
							body: formData
						})
						
						if (!response.ok) {
							throw new Error(`上传失败: ${response.status}`)
						}
						
						await response.json()
						// #endif
						
						// #ifdef MP-WEIXIN
						// 微信小程序环境
						await uploadChunk(
							this.uploadId,
							chunk.index,
							chunk.chunk,
							`chunk_${chunk.index}`
						)
						// #endif
						
						// 更新进度
						this.uploadedSize += (chunk.end - chunk.start)
						this.uploadProgress = Math.round((this.uploadedSize / this.totalSize) * 100)
						
						// 更新当前分片索引
						this.currentChunkIndex = i + 1
					} catch (error) {
						console.error(`分片 ${i} 上传失败:`, error)
						throw new Error(`分片 ${i + 1}/${totalChunks} 上传失败`)
					}
				}
			},
			
			// 复制取件码
			copyPickupCode() {
				// #ifdef H5
				if (navigator.clipboard) {
					navigator.clipboard.writeText(this.pickupCode).then(() => {
						uni.showToast({
							title: '已复制到剪贴板',
							icon: 'success'
						})
					})
				} else {
					// 降级方案
					const textarea = document.createElement('textarea')
					textarea.value = this.pickupCode
					document.body.appendChild(textarea)
					textarea.select()
					document.execCommand('copy')
					document.body.removeChild(textarea)
					uni.showToast({
						title: '已复制到剪贴板',
						icon: 'success'
					})
				}
				// #endif
				
				// #ifdef MP-WEIXIN
				uni.setClipboardData({
					data: this.pickupCode,
					success: () => {
						uni.showToast({
							title: '已复制到剪贴板',
							icon: 'success'
						})
					}
				})
				// #endif
			},
			
			// 返回首页
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
		background: #f5f5f5;
		padding: 40rpx;
	}
	
	.header {
		margin-bottom: 40rpx;
	}
	
	.title {
		font-size: 48rpx;
		font-weight: bold;
		color: #333333;
	}
	
	.upload-section {
		background: #ffffff;
		border-radius: 24rpx;
		padding: 60rpx 40rpx;
		box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.05);
	}
	
	.upload-area {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 100rpx 40rpx;
		border: 4rpx dashed #cccccc;
		border-radius: 16rpx;
		background: #fafafa;
	}
	
	.upload-icon {
		font-size: 100rpx;
		margin-bottom: 30rpx;
	}
	
	.upload-text {
		font-size: 32rpx;
		color: #333333;
		margin-bottom: 20rpx;
	}
	
	.upload-hint {
		font-size: 24rpx;
		color: #999999;
	}
	
	.file-info {
		display: flex;
		flex-direction: column;
		gap: 30rpx;
	}
	
	.file-item {
		display: flex;
		flex-direction: column;
		gap: 10rpx;
		padding: 30rpx;
		background: #f5f5f5;
		border-radius: 12rpx;
	}
	
	.file-name {
		font-size: 30rpx;
		color: #333333;
		word-break: break-all;
	}
	
	.file-size {
		font-size: 26rpx;
		color: #999999;
	}
	
	.upload-btn {
		width: 100%;
		height: 88rpx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		color: #ffffff;
		border: none;
		border-radius: 12rpx;
		font-size: 32rpx;
	}
	
	.upload-btn[disabled] {
		opacity: 0.6;
	}
	
	.upload-progress {
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
	
	.pickup-code-section {
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
	
	.pickup-code-box {
		width: 100%;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 20rpx;
		padding: 40rpx;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		border-radius: 16rpx;
	}
	
	.pickup-label {
		font-size: 28rpx;
		color: rgba(255, 255, 255, 0.8);
	}
	
	.pickup-code {
		font-size: 48rpx;
		font-weight: bold;
		color: #ffffff;
		letter-spacing: 8rpx;
		padding: 20rpx 40rpx;
		background: rgba(255, 255, 255, 0.2);
		border-radius: 12rpx;
	}
	
	.pickup-hint {
		font-size: 24rpx;
		color: rgba(255, 255, 255, 0.8);
		text-align: center;
	}
	
	.back-btn {
		width: 100%;
		height: 88rpx;
		background: #f5f5f5;
		color: #333333;
		border: none;
		border-radius: 12rpx;
		font-size: 32rpx;
	}
</style>
