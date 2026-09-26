# Tuyin-Suite · 图隐套件（StegoTank）

> 把秘密，藏进一张图片 —— 图片隐写算法聚合的原生 Android 应用。

![platform](https://img.shields.io/badge/Platform-Android%207.0%2B-3DDC84)
![lang](https://img.shields.io/badge/Language-Kotlin%20%2B%20Compose-7F52FF)
![license](https://img.shields.io/badge/License-GPL--3.0-or--later-blue)

图隐套件是一个把 **LSB 隐写 / RAC 图隐 / 幻影坦克 / 光棱坦克 / 图片混淆** 等图片隐写与混淆算法整合进一个原生 Android 应用的聚合项目。所有处理均在本地完成，图片不会上传到任何服务器。

本仓库是对以下开源项目的 **App 化重构**（Compose + Miuix 原生实现，无 WebView），算法与实现致敬原作者：

| 模块 | 上游项目 | 原作者 |
| --- | --- | --- |
| LSB 隐写（文本 / 文件藏进像素末位，可选 AES 加密） | [RobinDavid/LSB-Steganography](https://github.com/RobinDavid/LSB-Steganography) · [aagarwal1012/Image-Steganography-Library-Android](https://github.com/aagarwal1012/Image-Steganography-Library-Android) | Robin David · aagarwal1012 |
| RAC 图隐（一张图藏进另一张图） | [rac-hide](https://github.com/tuoPzf/rac-hide) | [tuoPzf](https://github.com/tuoPzf) |
| 光棱坦克（棱镜级光学变换隐写） | [Mirage_Decode](https://github.com/TankFactory/Mirage_Decode) | TankFactory |
| 幻影坦克（同图随观察方式呈现不同画面） | [Mirage_Colored](https://github.com/TankFactory/Mirage_Colored) | TankFactory |
| 图片混淆（Arnold 置乱等 8+ 算法） | [ObfuscationUtils](https://github.com/2195517546/ObfuscationUtils) | 2195517546 |

UI 组件基于 [compose-miuix-ui/miuix](https://github.com/compose-miuix-ui/miuix)，过渡动画参考 [ReSukiSU/ReSukiSU](https://github.com/ReSukiSU/ReSukiSU) 与 [KiminonawaResa/HyperLight](https://github.com/KiminonawaResa/HyperLight)。

## 功能

| 模块 | 说明 |
| --- | --- |
| LSB 隐写 | 把文字或任意文件写进 PNG 像素末 2 位（RGB 三通道共 6bit/像素），支持可选 AES 加密；实时容量进度池提示，超出变红 |
| 隐写检测 | 扫描图片判断是否疑似藏入数据：LSB 魔数 / 位流随机化 / 幻影 alpha 通道 / 光棱对比 / RAC 分块痕迹，结果分级「确定 / 疑似 / 未发现」，仅作判断提示 |
| 嵌入历史 | 自动记录每次 LSB 嵌入（时间 / 类型 / 内容 / 大小 / 是否加密），最多 50 条，可查看全文与清空 |
| RAC 图隐 | 封面 + 秘密图嵌入 / 提取；画质与容量实时预计算；封面增强；自定义参数；通道模拟（缩放 + JPEG 重压缩验证鲁棒性） |
| 幻影坦克 | 灰度 / 彩色模式，白底 / 黑底 / 棋盘格预览 |
| 光棱坦克 | 双权重合成，显影阈值与曝光调节，6 种显影覆盖 |
| 图片混淆 | Arnold 置乱 / 分块 / 行加密 / 行列 / 排序 / 随机等 8+ 算法，含质量档位 |

## 特性

- **原生 Compose + Miuix**（MIUI 风格），无 WebView
- **主题色全局联动**：莫奈取色（跟随壁纸）+ 6 种手动色（含瓷白），卡片 / 背景 / 导航同步变色
- **预测性返回**手势 + 跟手过渡动画
- **三语界面**：简体 / 繁体 / English
- 主题、语言、预返回设置持久化（SharedPreferences）
- 全部本地计算，无任何网络请求，不上传图片

## 应用信息

- 包名：`com.setgo.tank`
- 应用名：图隐套件 / StegoTank
- 当前正式版：`v1.0.4`（versionCode 64）
- 开发版：`v0.2.0.x.bate` 本地自用迭代（versionCode 递增，不发布）；更新日志见 [Releases](https://github.com/Gz3192019/Tuyin-Suite/releases) 与 [RELEASE_NOTES.md](RELEASE_NOTES.md)

## 构建

### 方式一：GitHub Actions（推荐）

仓库已内置 [.github/workflows/build-apk.yml](.github/workflows/build-apk.yml)：

1. 推送到 `main` 分支并打 tag `v1.0.x`（如 `v1.0.4`），或
2. 在 Actions 页面手动触发 `Build & Release APK`。

工作流会自动编译 APK、生成版本号并发布到 GitHub Releases。

### 方式二：本地构建

```bash
# 环境：JDK 21+（开发机为 JDK 22）+ Gradle 9.6（wrapper 已内置）
cd android-compose
./gradlew assembleRelease
# 产物：app/build/outputs/apk/release/app-release.apk
```

## 隐私与安全

隐写 ≠ 加密。本工具用于研究、版权水印与合法隐蔽通信；除 LSB 可选的 AES 加密外，各模块不具备密码学意义上的保密性，请勿用于保护真正敏感的信息。详见 [SECURITY.md](SECURITY.md)。

## 贡献

欢迎提交 Issue 与 PR，请阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 与 [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)。

## 许可

[GPL-3.0-or-later](LICENSE) · 尊重各上游项目的开源许可与原作者署名。
