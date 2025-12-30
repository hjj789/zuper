"use strict";
const common_vendor = require("../../common/vendor.js");
const utils_api = require("../../utils/api.js");
const utils_fileUtil = require("../../utils/fileUtil.js");
const _sfc_main = {
  data() {
    return {
      pickupCode: "",
      fileInfo: null,
      downloading: false,
      downloadComplete: false,
      downloadProgress: 0,
      downloadedSize: 0,
      totalSize: 0,
      loading: false,
      showCaptcha: false,
      captchaId: "",
      captchaCode: "",
      captchaDisplay: ""
    };
  },
  computed: {
    canQuery() {
      return this.pickupCode.length === 6;
    }
  },
  methods: {
    // 輸入變化
    onInputChange(e) {
      this.pickupCode = e.detail.value.toUpperCase();
      if (this.pickupCode.length === 6 && !this.showCaptcha) {
        this.loadCaptcha();
      }
    },
    // 加載驗證碼
    async loadCaptcha() {
      try {
        const result = await utils_api.getCaptcha();
        this.captchaId = result.captchaId;
        this.captchaDisplay = result.captchaCode;
        this.showCaptcha = true;
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/download/download.vue:131", "加載驗證碼失敗:", error);
      }
    },
    // 刷新驗證碼
    refreshCaptcha() {
      this.captchaCode = "";
      this.loadCaptcha();
    },
    // 查詢文件信息
    async queryFile() {
      if (!this.canQuery) {
        common_vendor.index.showToast({
          title: "請輸入6位取件碼",
          icon: "none"
        });
        return;
      }
      if (this.showCaptcha && !this.captchaCode) {
        common_vendor.index.showToast({
          title: "請輸入驗證碼",
          icon: "none"
        });
        return;
      }
      this.loading = true;
      common_vendor.index.showLoading({
        title: "查詢中..."
      });
      try {
        let result;
        if (this.showCaptcha) {
          result = await utils_api.getFileInfoWithCaptcha(this.pickupCode, this.captchaId, this.captchaCode);
        } else {
          result = await utils_api.getFileInfo(this.pickupCode);
        }
        this.fileInfo = result.data || result;
        this.totalSize = this.fileInfo.fileSize;
        common_vendor.index.hideLoading();
        common_vendor.index.showToast({
          title: "查詢成功",
          icon: "success"
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/download/download.vue:181", "查詢失敗:", error);
        common_vendor.index.hideLoading();
        if (error.message && error.message.includes("驗證碼")) {
          this.refreshCaptcha();
        }
        common_vendor.index.showToast({
          title: "查詢失敗: " + (error.message || "取件碼不存在"),
          icon: "none",
          duration: 3e3
        });
      } finally {
        this.loading = false;
      }
    },
    // 開始下載
    async startDownload() {
      if (!this.fileInfo) {
        return;
      }
      this.downloading = true;
      this.downloadProgress = 0;
      this.downloadedSize = 0;
      try {
        const downloadUrl = utils_api.getDownloadUrl(this.pickupCode);
        common_vendor.index.downloadFile({
          url: downloadUrl,
          success: (res) => {
            if (res.statusCode === 200) {
              common_vendor.index.saveFile({
                tempFilePath: res.tempFilePath,
                success: (saveRes) => {
                  common_vendor.index.showToast({
                    title: "文件已保存",
                    icon: "success"
                  });
                  this.downloading = false;
                  this.downloadComplete = true;
                }
              });
            }
          }
        });
        this.downloading = false;
        this.downloadComplete = true;
        common_vendor.index.showToast({
          title: "下載完成",
          icon: "success"
        });
      } catch (error) {
        common_vendor.index.__f__("error", "at pages/download/download.vue:279", "下載失敗:", error);
        this.downloading = false;
        common_vendor.index.showToast({
          title: "下載失敗: " + (error.message || "未知錯誤"),
          icon: "none",
          duration: 3e3
        });
      }
    },
    // 返回首頁
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
    a: !$data.fileInfo
  }, !$data.fileInfo ? common_vendor.e({
    b: common_vendor.o([($event) => $data.pickupCode = $event.detail.value, (...args) => $options.onInputChange && $options.onInputChange(...args)]),
    c: $data.pickupCode,
    d: $data.showCaptcha
  }, $data.showCaptcha ? {
    e: $data.captchaCode,
    f: common_vendor.o(($event) => $data.captchaCode = $event.detail.value),
    g: common_vendor.t($data.captchaDisplay),
    h: common_vendor.o((...args) => $options.refreshCaptcha && $options.refreshCaptcha(...args))
  } : {}, {
    i: common_vendor.t($data.loading ? "查詢中..." : "查詢文件"),
    j: common_vendor.o((...args) => $options.queryFile && $options.queryFile(...args)),
    k: !$options.canQuery || $data.loading
  }) : {}, {
    l: $data.fileInfo && !$data.downloading
  }, $data.fileInfo && !$data.downloading ? common_vendor.e({
    m: common_vendor.t($data.fileInfo.fileName),
    n: common_vendor.t($options.formatFileSize($data.fileInfo.fileSize)),
    o: $data.fileInfo.uploadTime
  }, $data.fileInfo.uploadTime ? {
    p: common_vendor.t($data.fileInfo.uploadTime)
  } : {}, {
    q: common_vendor.o((...args) => $options.startDownload && $options.startDownload(...args))
  }) : {}, {
    r: $data.downloading
  }, $data.downloading ? {
    s: common_vendor.t($data.downloadProgress),
    t: $data.downloadProgress,
    v: common_vendor.t($options.formatFileSize($data.downloadedSize)),
    w: common_vendor.t($options.formatFileSize($data.totalSize))
  } : {}, {
    x: $data.downloadComplete
  }, $data.downloadComplete ? {
    y: common_vendor.o((...args) => $options.goBack && $options.goBack(...args))
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-0927d71d"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/download/download.js.map
