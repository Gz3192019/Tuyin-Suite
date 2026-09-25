# Tuyin-Suite · 图隐套件 更新日志

版本号规则：每次发布的 APK 版本号都会迭代；正式版采用 `v1.0.x`，功能迭代期采用 `v0.1.x.beat`；Release 更新日志使用本文件全文。

---

## v1.0.0（正式版 · 2026-09-26）

**图隐套件正式版**：RAC 图隐 / 幻影坦克 / 光棱坦克 / 图片混淆 四合一聚合 App 首版正式发布。

### 新增与变化（相对全部 beat 版）

- **正式版发布**：版本号规则切换到 `v1.0.x`；`versionCode 43`，包名 `com.setgo.tank`，应用名 StegoTank
- **统一签名**：release / debug 共用仓库内 keystore（与历史 beat 版签名一致），从此所有安装包签名一致，可覆盖安装免卸载
- **GitHub Actions 云端构建**：新增 `.github/workflows/build-apk.yml`，推送 `v*` tag 或手动触发即可在云端编译 APK 并自动发布到 Releases

### 完整功能清单（本版包含）

| 模块 | 能力 |
| --- | --- |
| RAC 图隐 | 封面 + 秘密图嵌入 / 提取；画质与容量实时预计算；封面增强；自定义参数；通道模拟（缩放 + JPEG 重压缩验证鲁棒性）；结果保存 |
| 幻影坦克 | 灰度 / 彩色模式；白底 / 黑底 / 棋盘格预览；质量档位 |
| 光棱坦克 | 双权重合成；显影阈值与曝光调节；6 种显影覆盖；灰度 / 彩色 / 正反向 |
| 图片混淆 | Arnold 置乱 / 番茄 / 分块 / 行像素 / 像素级 / 行加密 / 行列 / 排序 / 随机 8+ 算法；质量档位（默认 0.95） |

### UI 与交互

- Compose + Miuix 原生实现（无 WebView），MIUI X 风格
- 主页四功能卡片 + 液态玻璃悬浮导航（功能区 / 关于区）
- 关于页：开发者（gz319）、开源致敬（含各上游作者头像）、技术栈说明、软件作用阐述
- 主题色全局联动：莫奈取色（跟随壁纸）+ 6 种手动色，卡片 / 背景 / 导航同步变色；明暗模式跟随系统
- 预测性返回手势 + 跟手过渡动画（AospSuiteTransition）
- 三语界面：简体 / 繁体 / English；设置项持久化（SharedPreferences）

### 修复与打磨（历次 beat 迭代汇总）

- 修复提取流程中秘密图无法点击、结果图片无法保存等问题
- 图片卡片跟随原图比例自适应（不再强制裁切 / 溢出）
- 顶部渐隐透明占位与功能区分隔优化
- 悬浮导航选中遮罩改为液态玻璃效果，未选中半透明
- 组件投影圆角一致、全局配色统一（瓷白底 #F5F5F7）
- 可用容量预计算、进度条联动
- 封面增强失效、原图比例失效等遗留问题修复
- 头像资源本地打包，离线可用

### 隐私与安全

- 全部算法本地执行，图片不上传任何服务器
- 隐写 ≠ 加密：本工具用于研究、版权水印与合法隐蔽通信，不具备密码学意义上的保密性；敏感信息请使用正规加密工具

---

## v0.1.x.beat（开发迭代期 · 已完成使命）

从 WebView 壳到原生重构的完整开发历程，均归档于历史 Releases 中（用户可自行管理删除不需要的 APK）。主要里程碑：

- **v0.1.0**：首版 WebView 壳 APK（图隐），页面嵌入 + 悬浮导航
- **v0.1.x 中期**：脱离 WebView，原生组件化重构；UI 打磨（组件投影、卡片对齐、顶部渐隐、底部液态玻璃导航、弧形描边、滚动条隐藏）
- **v0.1.3x 后期**：莫奈取色主题联动、预测性返回动画、三语界面、头像本地打包、关于页开源致敬
- **v0.1.39.beat**：v1.0.0 前最后一个 beat 版，功能齐备基线

---

## 关于

图隐套件是以下开源项目的 **App 化重构**（Compose + Miuix 原生实现），算法与实现致敬原作者：

- [rac-hide](https://github.com/tuoPzf/rac-hide) · [tuoPzf](https://github.com/tuoPzf) —— RAC 图隐
- [Mirage_Decode](https://github.com/TankFactory/Mirage_Decode) · TankFactory —— 光棱坦克
- [Mirage_Colored](https://github.com/TankFactory/Mirage_Colored) · TankFactory —— 幻影坦克
- [ObfuscationUtils](https://github.com/2195517546/ObfuscationUtils) · 2195517546 —— 图片混淆
- [compose-miuix-ui/miuix](https://github.com/compose-miuix-ui/miuix) —— UI 组件
- [ReSukiSU/ReSukiSU](https://github.com/ReSukiSU/ReSukiSU) · [KiminonawaResa/HyperLight](https://github.com/KiminonawaResa/HyperLight) —— 过渡动画参考

详见仓库 [README.md](README.md) 与 [SECURITY.md](SECURITY.md)。
