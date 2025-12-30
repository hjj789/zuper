"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  data() {
    return {};
  },
  onLoad() {
  },
  methods: {
    goToUpload() {
      common_vendor.index.navigateTo({
        url: "/pages/upload/upload"
      });
    },
    goToDownload() {
      common_vendor.index.navigateTo({
        url: "/pages/download/download"
      });
    }
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o((...args) => $options.goToUpload && $options.goToUpload(...args)),
    b: common_vendor.o((...args) => $options.goToDownload && $options.goToDownload(...args))
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-1cf27b2a"]]);
wx.createPage(MiniProgramPage);
//# sourceMappingURL=../../../.sourcemap/mp-weixin/pages/index/index.js.map
