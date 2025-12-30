"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const utils_fileUtil = require("../../utils/fileUtil.js");
const _sfc_main = {
  data() {
    return {
      selectedFile: null,
      uploading: false,
      uploadComplete: false,
      uploadProgress: 0,
      uploadedSize: 0,
      totalSize: 0,
      pickupCode: "",
      uploadId: "",
      chunks: [],
      currentChunkIndex: 0
    };
  },
  methods: {
    // 选择文件
    selectFile() {
      common_vendor.index.chooseFile({
        count: 1,
        success: (res) => {
          const file = res.tempFiles[0];
          this.selectedFile = {
            name: file.name,
            size: file.size,
            path: file.path
          };
          this.totalSize = file.size;
        },
        fail: (err) => {
          common_vendor.index.showToast({
            title: "选择文件失败",
            icon: "none"
          });
        }
      });
    },
    // 开始上传
    async startUpload() {
      var _a, _b;
      if (!this.selectedFile) {
        common_vendor.index.showToast({
          title: "请先选择文件",
          icon: "none"
        });
        return;
      }
      this.uploading = true;
      this.uploadProgress = 0;
      this.uploadedSize = 0;
      try {
        const fileId = utils_fileUtil.calculateFileId(this.selectedFile);
        const initResult = await utils_api.initUpload(
          this.selectedFile.name,
          this.selectedFile.size,
          fileId
        );
        this.uploadId = initResult.uploadId || ((_a = initResult.data) == null ? void 0 : _a.uploadId);
        await this.uploadChunks();
        const completeResult = await utils_api.completeUpload(this.uploadId);
        this.pickupCode = completeResult.pickupCode || ((_b = completeResult.data) == null ? void 0 : _b.pickupCode);
        this.uploading = false;
        this.uploadComplete = true;
        common_vendor.index.showToast({
          title: "上传成功",
          icon: "success"
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/upload/upload.vue:149", "上传失败:", error);
        this.uploading = false;
        common_vendor.index.showToast({
          title: "上传失败: " + (error.message || "未知错误"),
          icon: "none",
          duration: 3e3
        });
      }
    },
    // 分片上传
    async uploadChunks() {
      this.chunks = await utils_fileUtil.splitFile(this.selectedFile);
      const totalChunks = this.chunks.length;
      for (let i = 0; i < this.chunks.length; i++) {
        const chunk = this.chunks[i];
        try {
          await utils_api.uploadChunk(
            this.uploadId,
            chunk.index,
            chunk.chunk,
            `chunk_${chunk.index}`
          );
          this.uploadedSize += chunk.end - chunk.start;
          this.uploadProgress = Math.round(this.uploadedSize / this.totalSize * 100);
          this.currentChunkIndex = i + 1;
        } catch (error) {
          common_vendor.index.__f__("error", "at pages/upload/upload.vue:207", `分片 ${i} 上传失败:`, error);
          throw new Error(`分片 ${i + 1}/${totalChunks} 上传失败`);
        }
      }
    },
    // 复制取件码
    copyPickupCode() {
      common_vendor.index.setClipboardData({
        data: this.pickupCode,
        success: () => {
          common_vendor.index.showToast({
            title: "已复制到剪贴板",
            icon: "success"
          });
        }
      });
    },
    // 返回首页
    goBack() {
      common_vendor.index.reLaunch({
        url: "/pages/index/index"
      });
    },
    // 格式化文件大小
    formatFileSize: utils_fileUtil.formatFileSize
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: !$data.selectedFile
  }, !$data.selectedFile ? {
    b: common_vendor.o((...args) => $options.selectFile && $options.selectFile(...args))
  } : {}, {
    c: $data.selectedFile && !$data.uploading && !$data.uploadComplete
  }, $data.selectedFile && !$data.uploading && !$data.uploadComplete ? {
    d: common_vendor.t($data.selectedFile.name),
    e: common_vendor.t($options.formatFileSize($data.selectedFile.size)),
    f: common_vendor.o((...args) => $options.startUpload && $options.startUpload(...args)),
    g: $data.uploading
  } : {}, {
    h: $data.uploading
  }, $data.uploading ? {
    i: common_vendor.t($data.uploadProgress),
    j: $data.uploadProgress,
    k: common_vendor.t($options.formatFileSize($data.uploadedSize)),
    l: common_vendor.t($options.formatFileSize($data.totalSize))
  } : {}, {
    m: $data.uploadComplete
  }, $data.uploadComplete ? {
    n: common_vendor.t($data.pickupCode),
    o: common_vendor.o((...args) => $options.copyPickupCode && $options.copyPickupCode(...args)),
    p: common_vendor.o((...args) => $options.goBack && $options.goBack(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-aa5cff34"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/upload/upload.js.map
