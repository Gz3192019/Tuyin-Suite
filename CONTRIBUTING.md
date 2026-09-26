# 贡献指南 Contributing

欢迎参与图隐套件（Tuyin Suite）的开发。无论是修 bug、加功能、写文档还是提建议，都感谢你的投入。

## 起步

1. Fork 本仓库，clone 到本地
2. 确认目标模块：
   - `android-compose/` —— Android App 源码（Compose + Miuix 原生组件）
3. 新建分支：`git checkout -b feature/xxx`

## 提交规范

- 提交信息简洁明了，动词开头（如 `fix: 修复提取时图片显示不全`）
- 一次提交只做一个逻辑变更，便于回溯
- 涉及算法改动时，在提交信息中注明参考的上游实现（见 README 致敬列表）

## 代码风格

### Android

- 原生组件（无 WebView），延续 MIUI X 风格：瓷白底、圆角卡片、悬浮导航、莫奈取色
- 大图片处理必须放后台线程，捕获 `OutOfMemoryError` 并给出可操作提示
- 新增算法类参照 `PhantomTank.java` / `PrismTank.java` 的注释风格（原理 + 实现说明）

## 提 PR

1. 描述改动内容与动机，附上必要的截图（UI 改动必附）
2. 说明验证方式（Web：浏览器实测；Android：真机安装）
3. 引用受影响的 issue（如有）

## 行为准则

参与即默认遵守 [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)。
