# Zalith Launcher 2
![Downloads](https://img.shields.io/github/downloads/ZalithLauncher/ZalithLauncher2/total)
[![QQ](https://img.shields.io/badge/QQ-blue)](https://qm.qq.com/q/2MVxS0B29y)
[![Sponsor](https://img.shields.io/badge/sponsor-30363D?logo=GitHub-Sponsors)](https://afdian.com/a/MovTery)

[简体中文](README_ZH_CN.md)  

**Zalith Launcher 2** is a newly designed launcher for **Android devices** tailored for [Minecraft: Java Edition](https://www.minecraft.net/). The project uses [PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/tree/v3_openjdk/app_pojavlauncher/src/main/jni) as its core launching engine and features a modern UI built with **Jetpack Compose** and **Material Design 3**.
We are currently building our official website [zalithlauncher.cn](https://zalithlauncher.cn)  
Additionally, we are aware that a third-party website has been set up using the name “Zalith Launcher,” appearing to be official. Please note: **this site was not created by us**. It exploits the name to display ads for profit. We **do not participate in, endorse, or trust** such content.  
Please stay vigilant and **protect your personal privacy**!  

> [!WARNING]
> This project is **completely separate** from [ZalithLauncher](https://github.com/ZalithLauncher/ZalithLauncher).  
> The project is in an early development stage. Many features are still under construction—stay tuned for updates!

## 📅 Development Progress

Here’s the current roadmap of features and development status:

### ✅ Completed Features

* [x] Core launcher framework (themes, animations, settings, etc.)
* [x] Game launching and rendering
* [x] Game version download and installation
* [x] Control support: virtual mouse pointer / physical mouse & keyboard / gesture control
* [x] Version management: list, overview, and configuration
* [x] Customizable game installation directory
* [x] Account system: Microsoft OAuth login, offline accounts, and authentication server support
* [x] Java runtime management

### 🛠️ In Development / Planned Features

* [ ] Full control system (custom layout editor, control profile manager, etc.)
* [ ] Game version download extensions:
    * [ ] Install OptiFine with OptiFabric and Fabric
    * [ ] Install Fabric with the Fabric API mod
    * [ ] Install Quilt with the Quilted Fabric API mod
* [ ] Modpack download and automatic installation
* [ ] Mod download and automatic installation
* [ ] Resource pack download and automatic installation
* [ ] World save download and installation
* [ ] Shader pack download and automatic installation
* [ ] Content managers: UI for managing mods / resource packs / worlds / shaders
* [ ] Gamepad control support

## 🌐 Language and Translation Support

Zalith Launcher 2 currently supports the following two languages:

* **English** (default)
* **Simplified Chinese**
* **Turkish**

Added Turkish language support by Star1xr.

These two languages are the ones that the project officially **maintains and guarantees completeness**. We welcome community contributions for translations in other languages, but please note the following:

### 📌 Why Only English and Simplified Chinese?

* The project is **internationally aimed**, so English is used as the default language; however, since the developer is not a native English speaker, the English content relies heavily on AI-assisted translation, which may contain minor inaccuracies.
* The developer [@MovTery](https://github.com/MovTery) is based in China, and can guarantee the quality and completeness of the **Simplified Chinese** translation.
* Due to resource limitations, the completeness of other language translations cannot be guaranteed at this time and will depend on community contributions.

### ✍️ How to Contribute Translations?

If you would like to see your native language supported in the project, feel free to submit translation files via Pull Requests. Please follow these steps:

1. **Copy the Default Language Files**

   * Default English translation file location:
     [`strings.xml`](./ZalithLauncher/src/main/res/values/strings.xml)
2. **Create Your Language Resource Directory**

   * For example, Traditional Chinese: `values-zh-rTW`, French: `values-fr`, Japanese: `values-ja`, etc.
3. **Translate the Content**

   * Translate the contents of `strings.xml` into your language, and make sure to keep all `name` attributes unchanged.
   * It is recommended to refer to the official Simplified Chinese version:
     [`strings.xml`](./ZalithLauncher/src/main/res/values-zh-rCN/strings.xml)
4. **Submit a Pull Request**

   * In the PR description, specify which language has been added and clarify the translation method (e.g., "human translation").

### ✅ Translation Guidelines and Notes

* **Do not use machine translation** (e.g., Google Translate, DeepL, etc.) to directly generate translations.
* Keep professional terminology and follow platform-specific expressions (e.g., Minecraft-related terms).
* Do not translate punctuation marks or key instructions (e.g., "Shift", "Ctrl", etc.).
* Ensure string integrity (placeholders like `%1$s`, `\n`, and similar formats must be retained).
* Please note that special characters in XML need to be escaped, such as `<` should be written as `&lt;`, and `&` should be written as `&amp;`.
   * For escape rules, refer to the [official XML escape rules](https://www.w3.org/TR/xml/#syntax).

Thank you to all the language contributors for making Zalith Launcher 2 more multilingual and global! 🎉

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
