"use strict";
const common_vendor = require("../common/vendor.js");
const utils_config = require("./config.js");
function request(url, method = "GET", data = {}, header = {}) {
  return new Promise((resolve, reject) => {
    common_vendor.index.request({
      url: utils_config.BASE_URL + url,
      method,
      data,
      header: {
        "Content-Type": "application/json",
        ...header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data);
        } else {
          reject(new Error(`請求失敗: ${res.statusCode}`));
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}
function initUpload(fileName, fileSize, fileMd5) {
  return request("/file/init", "POST", {
    fileName,
    fileSize,
    fileMd5
  });
}
function uploadChunk(uploadId, chunkIndex, chunk, chunkMd5) {
  return new Promise((resolve, reject) => {
    common_vendor.index.uploadFile({
      url: utils_config.BASE_URL + "/file/upload",
      filePath: chunk,
      name: "chunk",
      formData: {
        uploadId,
        chunkIndex,
        chunkMd5
      },
      success: (res) => {
        try {
          const data = JSON.parse(res.data);
          resolve(data);
        } catch (e) {
          resolve(res.data);
        }
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}
function completeUpload(uploadId) {
  return request("/file/complete", "POST", {
    uploadId
  });
}
function getCaptcha() {
  return request("/file/captcha", "GET");
}
function getFileInfo(pickupCode) {
  return request(`/file/info/${pickupCode}`, "GET");
}
function getFileInfoWithCaptcha(pickupCode, captchaId, captchaCode) {
  return request(`/file/info/${pickupCode}`, "POST", {
    captchaId,
    captchaCode
  });
}
function getDownloadUrl(pickupCode) {
  return `${utils_config.BASE_URL}/file/download/${pickupCode}`;
}
exports.completeUpload = completeUpload;
exports.getCaptcha = getCaptcha;
exports.getDownloadUrl = getDownloadUrl;
exports.getFileInfo = getFileInfo;
exports.getFileInfoWithCaptcha = getFileInfoWithCaptcha;
exports.initUpload = initUpload;
exports.uploadChunk = uploadChunk;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/api.js.map
