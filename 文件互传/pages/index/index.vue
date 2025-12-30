<template>
	<view class="container">
		<view class="header">
			<text class="title">大文件互传</text>
			<text class="subtitle">安全、快速、便捷的文件传输服务</text>
		</view>
		
		<view class="content">
			<view class="card upload-card" @click="goToUpload">
				<view class="card-icon">📤</view>
				<text class="card-title">上传文件</text>
				<text class="card-desc">上传大文件并获取取件码</text>
			</view>
			
			<view class="card download-card" @click="goToDownload">
				<view class="card-icon">📥</view>
				<text class="card-title">下载文件</text>
				<text class="card-desc">输入取件码下载文件</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				
			}
		},
		onLoad() {
			
		},
		methods: {
			goToUpload() {
				uni.navigateTo({
					url: '/pages/upload/upload'
				})
			},
			goToDownload() {
				uni.navigateTo({
					url: '/pages/download/download'
				})
			}
		}
	}
</script>

<style scoped>
	.container {
		min-height: 100vh;
		background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
		padding: 40rpx;
	}
	
	.header {
		text-align: center;
		margin-top: 100rpx;
		margin-bottom: 100rpx;
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
		color: rgba(255, 255, 255, 0.8);
	}
	
	.content {
		display: flex;
		flex-direction: column;
		gap: 40rpx;
	}
	
	.card {
		background: #ffffff;
		border-radius: 24rpx;
		padding: 60rpx 40rpx;
		box-shadow: 0 10rpx 40rpx rgba(0, 0, 0, 0.1);
		display: flex;
		flex-direction: column;
		align-items: center;
		transition: transform 0.3s;
	}
	
	.card:active {
		transform: scale(0.98);
	}
	
	.card-icon {
		font-size: 80rpx;
		margin-bottom: 30rpx;
	}
	
	.card-title {
		font-size: 36rpx;
		font-weight: bold;
		color: #333333;
		margin-bottom: 20rpx;
	}
	
	.card-desc {
		font-size: 28rpx;
		color: #999999;
		text-align: center;
	}
</style>
