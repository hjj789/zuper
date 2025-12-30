"use strict";
const common_vendor = require("../common/vendor.js");
const utils_config = require("./config.js");
function calculateFileId(file) {
  const timestamp = Date.now();
  return `${file.name}_${file.size}_${timestamp}`;
}
function splitFile(file) {
  return new Promise((resolve, reject) => {
    const fs = common_vendor.index.getFileSystemManager();
    const filePath = file.path || file.tempFilePath;
    fs.readFile({
      filePath,
      success: (res) => {
        const arrayBuffer = res.data;
        const chunks = [];
        const totalChunks = Math.ceil(arrayBuffer.byteLength / utils_config.CHUNK_SIZE);
        for (let i = 0; i < totalChunks; i++) {
          const start = i * utils_config.CHUNK_SIZE;
          const end = Math.min(start + utils_config.CHUNK_SIZE, arrayBuffer.byteLength);
          const chunkData = arrayBuffer.slice(start, end);
          const tempFilePath = `${common_vendor.index.env.USER_DATA_PATH}/chunk_${i}_${Date.now()}`;
          fs.writeFileSync(tempFilePath, chunkData);
          chunks.push({
            index: i,
            chunk: tempFilePath,
            start,
            end
          });
        }
        resolve(chunks);
      },
      fail: (err) => {
        reject(err);
      }
    });
  });
}
function formatFileSize(bytes) {
  if (bytes === 0)
    return "0 B";
  const k = 1024;
  const sizes = ["B", "KB", "MB", "GB", "TB"];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + " " + sizes[i];
}
exports.calculateFileId = calculateFileId;
exports.formatFileSize = formatFileSize;
exports.splitFile = splitFile;
//# sourceMappingURL=../../.sourcemap/mp-weixin/utils/fileUtil.js.map
