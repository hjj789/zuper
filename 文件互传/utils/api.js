/**
 * API 工具類
 * 用於與後端 Java 服務交互
 */

import { BASE_URL } from './config.js'

/**
 * 統一請求方法
 */
function request(url, method = 'GET', data = {}, header = {}) {
	return new Promise((resolve, reject) => {
		uni.request({
			url: BASE_URL + url,
			method: method,
			data: data,
			header: {
				'Content-Type': 'application/json',
				...header
			},
			success: (res) => {
				if (res.statusCode === 200) {
					resolve(res.data)
				} else {
					reject(new Error(`請求失敗: ${res.statusCode}`))
				}
			},
			fail: (err) => {
				reject(err)
			}
		})
	})
}

/**
 * 初始化文件上傳（獲取上傳任務ID）
 */
export function initUpload(fileName, fileSize, fileMd5) {
	return request('/file/init', 'POST', {
		fileName: fileName,
		fileSize: fileSize,
		fileMd5: fileMd5
	})
}

/**
 * 上傳文件分片
 */
export function uploadChunk(uploadId, chunkIndex, chunk, chunkMd5) {
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: BASE_URL + '/file/upload',
			filePath: chunk,
			name: 'chunk',
			formData: {
				uploadId: uploadId,
				chunkIndex: chunkIndex,
				chunkMd5: chunkMd5
			},
			success: (res) => {
				try {
					const data = JSON.parse(res.data)
					resolve(data)
				} catch (e) {
					resolve(res.data)
				}
			},
			fail: (err) => {
				reject(err)
			}
		})
	})
}

/**
 * 完成文件上傳（合併分片並生成取件碼）
 */
export function completeUpload(uploadId) {
	return request('/file/complete', 'POST', {
		uploadId: uploadId
	})
}

/**
 * 獲取驗證碼
 */
export function getCaptcha() {
	return request('/file/captcha', 'GET')
}

/**
 * 根據取件碼獲取文件信息（不帶驗證碼，兼容舊版本）
 */
export function getFileInfo(pickupCode) {
	return request(`/file/info/${pickupCode}`, 'GET')
}

/**
 * 根據取件碼獲取文件信息（帶驗證碼）
 */
export function getFileInfoWithCaptcha(pickupCode, captchaId, captchaCode) {
	return request(`/file/info/${pickupCode}`, 'POST', {
		captchaId: captchaId,
		captchaCode: captchaCode
	})
}

/**
 * 下載文件
 */
export function downloadFile(pickupCode) {
	return request(`/file/download/${pickupCode}`, 'GET')
}

/**
 * 獲取文件下載地址
 */
export function getDownloadUrl(pickupCode) {
	return `${BASE_URL}/file/download/${pickupCode}`
}

