# Tuyin-Suite / 图隐套件 (StegoTank)

图片隐写工具聚合 App —— 把多种图片隐写/混淆算法整合到一个原生 Android 应用中。

> 本仓库是对以下开源项目的 **App 化重构**（Compose + Miuix 原生实现），算法与实现致敬原作者：
> - [RAC-hide](https://github.com/TankFactory/RAC-hide) —— RAC 图隐（一张图藏进另一张图）
> - [Mirage_Decode](https://github.com/TankFactory/Mirage_Decode) —— 光棱坦克（棱镜级光学变换隐写）
> - [Mirage_Colored](https://github.com/TankFactory/Mirage_Colored) —— 幻影坦克（同一张图随观察方式呈现不同画面）
> - [ObfuscationUtils](https://github.com/2195517546/ObfuscationUtils) —— 图片混淆（Arnold 置乱等 8 种算法）

## 功能

| 模块 | 说明 |
| --- | --- |
| RAC 图隐 | 封面 + 秘密图嵌入，支持画质/容量调节与封面增强 |
| 幻影坦克 | 同一张图在不同亮度/观察方式下显示不同画面 |
| 光棱坦克 | 6 种摆放方式 + 灰度/彩色 + 正/反向 + 双阈值 + 6 种显影覆盖 |
| 图片混淆 | Arnold 置乱 / 番茄 / 分块 / 行像素 / 像素级 / 行加密 / 行列 / 排序 / 随机，8+ 算法 |

## 特性

- 原生 Compose + Miuix（MIUI 风格）UI，无 WebView
- 明暗模式跟随系统
- 主题色全局联动：莫奈取色（跟随壁纸）+ 6 种手动色，卡片/背景/导航全联动
- 预测性返回（预返回）手势 + 跟手过渡动画
- 简体 / 繁体 / English 三语界面
- 主题、语言、预返回设置持久化（SharedPreferences）

## 构建

```bash
# 环境：JDK 21 + Gradle 9.6（或更高）
cd android-compose
gradle assembleDebug
# 产物：app/build/outputs/apk/debug/app-debug.apk
```

- 包名：`com.setgo.tank`
- 应用名：StegoTank
- 当前开发版本：`v0.1.x.beat`（每个版本递增 versionCode，APK 与更新日志见 Releases）

## 许可

仅作学习与技术交流使用。请尊重各上游项目的开源许可与原作者署名。
