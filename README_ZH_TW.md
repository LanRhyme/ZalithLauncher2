# Zalith Launcher 2
![Downloads](https://img.shields.io/github/downloads/ZalithLauncher/ZalithLauncher2/total)
![Discord](https://img.shields.io/discord/1409012263423185039?logo=discord&label=Discord&color=7289DA&link=https%3A%2F%2Fdiscord.gg%2FyDDkTHp4cJ)
[![Sponsor](https://img.shields.io/badge/sponsor-30363D?logo=GitHub-Sponsors)](https://afdian.com/a/MovTery)
<!-- [![QQ](https://img.shields.io/badge/QQ-blue)](https://qm.qq.com/q/2MVxS0B29y) -->

[English](README.md) | [简体中文](README_ZH_CN.md)

**Zalith Launcher 2** 是一個全新設計、面向 **Android 裝置** 的 [Minecraft: Java Edition](https://www.minecraft.net/) 啟動器。專案使用 [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/tree/v3_openjdk/app_pojavlauncher/src/main/jni) 作為啟動核心，採用 **Jetpack Compose** 與 **Material Design 3** 構建現代化 UI 體驗。  
我們目前正在搭建自己的官方網站 [zalithlauncher.cn](https://zalithlauncher.cn)  
此外，我們已注意到有第三方使用「Zalith Launcher」名稱搭建了一個看似官方的網站。請注意：**該網站並非我們創建**，其透過冒用名義並植入廣告牟利。我們對此類行為**不參與、不認可、不信任**。  
請務必提高警覺，**謹防個人隱私資訊洩露**！  

> [!WARNING]
> 該專案與 [ZalithLauncher](https://github.com/ZalithLauncher/ZalithLauncher) 屬於兩個完全不同的專案  
> 專案目前仍處於早期開發階段，功能持續新增中，歡迎關注更新！





## 🌐 語言與翻譯支援

我們正在使用 Weblate 平台翻譯 Zalith Launcher 2，歡迎您前往我們的 [Weblate 專案](https://hosted.weblate.org/projects/zalithlauncher2) 參與翻譯！  
感謝每一位語言貢獻者的支持，讓 Zalith Launcher 2 更加多語化、更加國際化！




## 👨‍💻 開發者

該專案目前由 [@MovTery](https://github.com/MovTery) 獨立開發，歡迎提出建議或反饋問題。由於個人精力有限，部分功能可能實現較慢，敬請諒解！




## 📦 構建方式（開發者）

> 以下內容適用於希望參與開發或自行構建應用的使用者。

### 環境要求

* Android Studio Bumblebee 以上
* Android SDK：
    * **最低 API**：26（Android 8.0）
    * **目標 API**：35（Android 14）
* JDK 11

### 構建步驟

```bash
git clone git@github.com:ZalithLauncher/ZalithLauncher2.git
# 使用 Android Studio 開啟專案並進行構建
```




## 📜 License

本專案程式碼遵循 **[GPL-3.0 license](LICENSE)** 開源協議。

### 附加條款（依據 GPLv3 開源授權條款第七條）

1. 當你分發本程式的修改版本時，必須以合理方式修改該程式的名稱或版本號，以區別於原始版本。（依據 [GPLv3, 7(c)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L372-L374)）
    - 修改版本 **不得在名稱中包含原程式名稱「ZalithLauncher」或其縮寫「ZL」，亦不得使用與官方名稱相近、可能造成混淆的名稱**。
    - 所有修改版本 **必須在程式啟動畫面或主介面中以明顯方式標示其為「非官方修改版」**。
    - 程式的應用名稱可於 [gradle.properties](./ZalithLauncher/gradle.properties) 中進行修改。

2. 你不得移除本程式所顯示的版權聲明。（依據 [GPLv3, 7(b)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L368-L370)）

