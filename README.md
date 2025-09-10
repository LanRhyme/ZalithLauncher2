# Zalith Launcher 2
![Downloads](https://img.shields.io/github/downloads/ZalithLauncher/ZalithLauncher2/total)
![Discord](https://img.shields.io/discord/1409012263423185039?logo=discord&label=Discord&color=7289DA&link=https%3A%2F%2Fdiscord.gg%2FyDDkTHp4cJ)
[![Sponsor](https://img.shields.io/badge/sponsor-30363D?logo=GitHub-Sponsors)](https://afdian.com/a/MovTery)
<!-- [![QQ](https://img.shields.io/badge/QQ-blue)](https://qm.qq.com/q/2MVxS0B29y) -->

[简体中文](README_ZH_CN.md) | [繁體中文](README_ZH_TW.md)

**Zalith Launcher 2** is a newly designed launcher for **Android devices** tailored for [Minecraft: Java Edition](https://www.minecraft.net/). The project uses [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/tree/v3_openjdk/app_pojavlauncher/src/main/jni) as its core launching engine and features a modern UI built with **Jetpack Compose** and **Material Design 3**.  
We are currently building our official website [zalithlauncher.cn](https://zalithlauncher.cn)  
Additionally, we are aware that a third-party website has been set up using the name “Zalith Launcher,” appearing to be official. Please note: **this site was not created by us**. It exploits the name to display ads for profit. We **do not participate in, endorse, or trust** such content.  
Please stay vigilant and **protect your personal privacy**!  

> [!WARNING]
> This project is **completely separate** from [ZalithLauncher](https://github.com/ZalithLauncher/ZalithLauncher).  
> The project is in an early development stage. Many features are still under construction—stay tuned for updates!

## 🌐 Language and Translation Support

We are using the Weblate platform to translate Zalith Launcher 2. You're welcome to join our [Weblate project](https://hosted.weblate.org/projects/zalithlauncher2) and contribute to the translations!  
Thank you to every language contributor for helping make Zalith Launcher 2 more multilingual and global!

## 👨‍💻 Developer

This project is currently being developed solely by [@MovTery](https://github.com/MovTery).
Feedback, suggestions, and issue reports are very welcome. As it's a personal project, development may take time—thank you for your patience!

## 📦 Build Instructions (For Developers)

> The following section is for developers who wish to contribute or build the project locally.

### Requirements

* Android Studio **Bumblebee** or newer
* Android SDK:
  * **Minimum API level**: 26 (Android 8.0)
  * **Target API level**: 35 (Android 14)
* JDK 11

### Build Steps

```bash
git clone git@github.com:ZalithLauncher/ZalithLauncher2.git
# Open the project in Android Studio and build
```

## 📜 License

This project is licensed under the **[GPL-3.0 license](LICENSE)**.

### Additional Terms (Pursuant to Section 7 of the GPLv3 License)

1. When distributing a modified version of this program, you must reasonably modify the program's name or version number to distinguish it from the original version. (According to [GPLv3, 7(c)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L372-L374))
    - Modified versions **must not include the original program name "ZalithLauncher" or its abbreviation "ZL" in their name, nor use any name that is similar enough to cause confusion with the official name**.
    - All modified versions **must clearly indicate that they are “Unofficial Modified Versions” on the program’s startup screen or main interface**.
    - The application name of the program can be modified in [gradle.properties](./ZalithLauncher/gradle.properties).

2. You must not remove the copyright notices displayed by the program. (According to [GPLv3, 7(b)](https://github.com/ZalithLauncher/ZalithLauncher2/blob/969827b/LICENSE#L368-L370))

## Open Source Libraries and Licenses

This software uses the following open source libraries:

| Library                               | Copyright                                                                   | License              | Official Link                                                                     |
|---------------------------------------|-----------------------------------------------------------------------------|----------------------|-----------------------------------------------------------------------------------|
| androidx-constraintlayout-compose     | Copyright © The Android Open Source Project                                 | Apache 2.0           | [Link](https://developer.android.com/develop/ui/compose/layouts/constraintlayout) |
| androidx-material-icons-core          | Copyright © The Android Open Source Project                                 | Apache 2.0           | [Link](https://developer.android.com/jetpack/androidx/releases/compose-material)  |
| androidx-material-icons-extended      | Copyright © The Android Open Source Project                                 | Apache 2.0           | [Link](https://developer.android.com/jetpack/androidx/releases/compose-material)  |
| Apache Commons Codec                  | -                                                                           | Apache 2.0           | [Link](https://commons.apache.org/proper/commons-codec)                           |
| Apache Commons Compress               | -                                                                           | Apache 2.0           | [Link](https://commons.apache.org/proper/commons-compress)                        |
| Apache Commons IO                     | -                                                                           | Apache 2.0           | [Link](https://commons.apache.org/proper/commons-io)                              |
| ByteHook                              | Copyright © 2020-2024 ByteDance, Inc.                                       | MIT License          | [Link](https://github.com/bytedance/bhook)                                        |
| Coil Compose                          | Copyright © 2025 Coil Contributors                                          | Apache 2.0           | [Link](https://github.com/coil-kt/coil)                                           |
| Coil Gifs                             | Copyright © 2025 Coil Contributors                                          | Apache 2.0           | [Link](https://github.com/coil-kt/coil)                                           |
| colorpicker-compose                   | Copyright © 2022 skydoves (Jaewoong Eum)                                    | Apache 2.0           | [Link](https://github.com/skydoves/colorpicker-compose)                           |
| Gson                                  | Copyright © 2008 Google Inc.                                                | Apache 2.0           | [Link](https://github.com/google/gson)                                            |
| kotlinx.coroutines                    | Copyright © 2000-2020 JetBrains s.r.o.                                      | Apache 2.0           | [Link](https://github.com/Kotlin/kotlinx.coroutines)                              |
| ktor-client-cio                       | Copyright © 2000-2023 JetBrains s.r.o.                                      | Apache 2.0           | [Link](https://ktor.io)                                                           |
| ktor-client-content-negotiation       | Copyright © 2000-2023 JetBrains s.r.o.                                      | Apache 2.0           | [Link](https://ktor.io)                                                           |
| ktor-client-core                      | Copyright © 2000-2023 JetBrains s.r.o.                                      | Apache 2.0           | [Link](https://ktor.io)                                                           |
| ktor-http                             | Copyright © 2000-2023 JetBrains s.r.o.                                      | Apache 2.0           | [Link](https://ktor.io)                                                           |
| ktor-serialization-kotlinx-json       | Copyright © 2000-2023 JetBrains s.r.o.                                      | Apache 2.0           | [Link](https://ktor.io)                                                           |
| LWJGL - Lightweight Java Game Library | Copyright © 2012-present Lightweight Java Game Library All rights reserved. | BSD 3-Clause License | [Link](https://github.com/LWJGL/lwjgl3)                                           |
| material-color-utilities              | Copyright 2021 Google LLC                                                   | Apache 2.0           | [Link](https://github.com/material-foundation/material-color-utilities)           |
| Maven Artifact                        | Copyright © The Apache Software Foundation                                  | Apache 2.0           | [Link](https://github.com/apache/maven/tree/maven-3.9.9/maven-artifact)           |
| MMKV                                  | Copyright © 2018 THL A29 Limited, a Tencent company.                        | BSD 3-Clause License | [Link](https://github.com/Tencent/MMKV)                                           |
| Navigation 3                          | Copyright © The Android Open Source Project                                 | Apache 2.0           | [Link](https://developer.android.com/jetpack/androidx/releases/navigation3)       |
| NBT                                   | Copyright © 2016 - 2020 Querz                                               | MIT License          | [Link](https://github.com/Querz/NBT)                                              |
| OkHttp                                | Copyright © 2019 Square, Inc.                                               | Apache 2.0           | [Link](https://github.com/square/okhttp)                                          |
| proxy-client-android                  | -                                                                           | LGPL-3.0 License     | [Link](https://github.com/TouchController/TouchController)                        |
| StringFog                             | Copyright © 2016-2023, Megatron King                                        | Apache 2.0           | [Link](https://github.com/MegatronKing/StringFog)                                 |
| XZ for Java                           | Copyright © The XZ for Java authors and contributors                        | 0BSD License         | [Link](https://tukaani.org/xz/java.html)                                          |
