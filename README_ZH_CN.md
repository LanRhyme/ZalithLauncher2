# Zalith Launcher 2
![Downloads](https://img.shields.io/github/downloads/ZalithLauncher/ZalithLauncher2/total)
![Discord](https://img.shields.io/discord/1409012263423185039?logo=discord&label=Discord&color=7289DA&link=https%3A%2F%2Fdiscord.gg%2FyDDkTHp4cJ)
[![Sponsor](https://img.shields.io/badge/sponsor-30363D?logo=GitHub-Sponsors)](https://afdian.com/a/MovTery)
<!-- [![QQ](https://img.shields.io/badge/QQ-blue)](https://qm.qq.com/q/2MVxS0B29y) -->

[English](README.md) | [繁體中文](README_ZH_TW.md)

**Zalith Launcher 2** 是一个全新设计、面向 **Android 设备** 的 [Minecraft: Java Edition](https://www.minecraft.net/) 启动器。项目使用 [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/tree/v3_openjdk/app_pojavlauncher/src/main/jni) 作为启动核心，采用 **Jetpack Compose** 与 **Material Design 3** 构建现代化 UI 体验。  
我们目前正在搭建自己的官方网站 [zalithlauncher.cn](https://zalithlauncher.cn)  
此外，我们已注意到有第三方使用“Zalith Launcher”名称搭建了一个看似官方的网站。请注意：**该网站并非我们创建**，其通过冒用名义并植入广告牟利。我们对此类行为**不参与、不认可、不信任**。  
请务必提高警惕，**谨防个人隐私信息泄露**！  

> [!WARNING]
> 该项目与 [ZalithLauncher](https://github.com/ZalithLauncher/ZalithLauncher) 属于两个完全不同的项目  
> 项目目前仍处于早期开发阶段，功能持续添加中，欢迎关注更新！



## 🌐 语言与翻译支持

我们正在使用 Weblate 平台翻译 Zalith Launcher 2，欢迎您前往我们的 [Weblate 项目](https://hosted.weblate.org/projects/zalithlauncher2) 参与翻译！  
感谢每一位语言贡献者的支持，让 Zalith Launcher 2 更加多语、更加全球化！




## 👨‍💻 开发者

该项目目前由 [@MovTery](https://github.com/MovTery) 独立开发，欢迎提出建议或反馈问题。由于个人精力有限，部分功能可能实现较慢，敬请谅解！




## 📦 构建方式（开发者）

> 以下内容适用于希望参与开发或自行构建应用的用户。

### 环境要求

* Android Studio Bumblebee 以上
* Android SDK：
    * **最低 API**：26（Android 8.0）
    * **目标 API**：35（Android 14）
* JDK 11

### 构建步骤

```bash
git clone git@github.com:ZalithLauncher/ZalithLauncher2.git
# 使用 Android Studio 打开项目并进行构建
```




## 📜 License

本项目代码遵循 **[GPL-3.0 license](LICENSE)** 开源协议。

### 附加条款 (依据 GPLv3 开源协议第七条)  

1. 当你分发该程序的修改版本时，你必须以合理方式修改该程序的名称或版本号，以示其与原始版本不同。(依据 [GPLv3, 7(c)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L372-L374))
   - 修改版本 **不得在名称中包含原程序名称 “ZalithLauncher” 或其缩写 “ZL”，也不得使用与官方名称相近、可能导致混淆的名称**。
   - 所有修改版本 **必须在程序启动页面或主界面中以明显方式标注其为“非官方修改版”**。
   - 该程序的应用名称可在 [gradle.properties](./ZalithLauncher/gradle.properties) 中修改。

2. 你不得移除该程序所显示的版权声明。(依据 [GPLv3, 7(b)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L368-L370))

