/**
 * 文件工具类
 * 处理大文件分片上传
 */

import { CHUNK_SIZE } from './config.js'

/**
 * 计算文件 MD5（简化版，实际项目中建议使用更可靠的 MD5 库）
 * 注意：这里使用文件名+大小+时间戳作为临时标识，实际应使用真正的 MD5 算法
 */
export function calculateFileId(file) {
	// 在实际项目中，应该使用真正的 MD5 计算
	// 这里使用文件名+大小+时间戳作为临时标识
	const timestamp = Date.now()
	return `${file.name}_${file.size}_${timestamp}`
}

/**
 * 将文件分片
 */
export function splitFile(file) {
	return new Promise((resolve, reject) => {
		// #ifdef H5
		// H5 环境使用 File.slice
		const chunks = []
		const totalChunks = Math.ceil(file.size / CHUNK_SIZE)
		
		for (let i = 0; i < totalChunks; i++) {
			const start = i * CHUNK_SIZE
			const end = Math.min(start + CHUNK_SIZE, file.size)
			const chunk = file.slice(start, end)
			
			chunks.push({
				index: i,
				chunk: chunk,
				start: start,
				end: end
			})
		}
		
		resolve(chunks)
		// #endif
		
		// #ifdef MP-WEIXIN
		// 微信小程序环境
		const fs = uni.getFileSystemManager()
		const filePath = file.path || file.tempFilePath
		
		fs.readFile({
			filePath: filePath,
			success: (res) => {
				const arrayBuffer = res.data
				const chunks = []
				const totalChunks = Math.ceil(arrayBuffer.byteLength / CHUNK_SIZE)
				
				for (let i = 0; i < totalChunks; i++) {
					const start = i * CHUNK_SIZE
					const end = Math.min(start + CHUNK_SIZE, arrayBuffer.byteLength)
					const chunkData = arrayBuffer.slice(start, end)
					
					// 将 ArrayBuffer 转换为临时文件
					const tempFilePath = `${uni.env.USER_DATA_PATH}/chunk_${i}_${Date.now()}`
					fs.writeFileSync(tempFilePath, chunkData)
					
					chunks.push({
						index: i,
						chunk: tempFilePath,
						start: start,
						end: end
					})
				}
				
				resolve(chunks)
			},
			fail: (err) => {
				reject(err)
			}
		})
		// #endif
		
		// #ifndef H5 || MP-WEIXIN
		// 其他平台的处理
		reject(new Error('当前平台不支持文件分片'))
		// #endif
	})
}

/**
 * 计算分片的 MD5（简化版）
 */
export function calculateChunkMd5(chunk) {
	// 实际项目中应使用真正的 MD5 算法
	// 这里返回一个临时标识
	return `chunk_${chunk.index}_${Date.now()}`
}

/**
 * 格式化文件大小
 */
export function formatFileSize(bytes) {
	if (bytes === 0) return '0 B'
	const k = 1024
	const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
	const i = Math.floor(Math.log(bytes) / Math.log(k))
	return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

/**
 * 格式化上传进度百分比
 */
export function formatProgress(loaded, total) {
	if (total === 0) return 0
	return Math.round((loaded / total) * 100)
}

