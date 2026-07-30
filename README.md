# LSPatch Framework

[![Build](https://img.shields.io/github/actions/workflow/status/LSPosed/LSPatch/main.yml?branch=master&logo=github&label=Build&event=push)](https://github.com/LSPosed/LSPatch/actions/workflows/main.yml?query=event%3Apush+is%3Acompleted+branch%3Amaster) [![Crowdin](https://img.shields.io/badge/Localization-Crowdin-blueviolet?logo=Crowdin)](https://lsposed.crowdin.com/lspatch) [![Download](https://img.shields.io/github/v/release/LSPosed/LSPatch?color=orange&logoColor=orange&label=Download&logo=DocuSign)](https://github.com/LSPosed/LSPatch/releases/latest) [![Total](https://shields.io/github/downloads/LSPosed/LSPatch/total?logo=Bookmeter&label=Counts&logoColor=yellow&color=yellow)](https://github.com/LSPosed/LSPatch/releases)

## 简介

LSPatch(ds) 是一款基于 LSPosed 核心的社区维护版免 Root Xposed 框架，通过向目标 APK 中注入 dex 和 so 文件来集成 Xposed API。本项目是原 LSPatch 的社区延续版本，仓库地址 [LQS686/LSPatch](https://github.com/LQS686/LSPatch)。

## 支持版本

- 最低：Android 9
- 最高：理论上与 [LSPosed](https://github.com/LSPosed/LSPosed#supported-versions) 相同

## 下载

稳定版请前往 [GitHub Releases 页面](https://github.com/LQS686/LSPatch/releases)  
 Canary 构建请查看 [GitHub Actions](https://github.com/LQS686/LSPatch/actions)  
注意：debug 构建仅在 GitHub Actions 中提供  

## 使用方法

+ 通过 jar 使用
1. 下载 `lspatch.jar`
1. 运行 `java -jar lspatch.jar`

+ 通过管理器使用
1. 在 Android 设备上下载并安装 `manager.apk`
1. 按照管理器应用的指引操作

## 参与翻译

您可以在[这里](https://lsposed.crowdin.com/lspatch)贡献翻译。

## 鸣谢

- [LSPosed](https://github.com/LSPosed/LSPosed)：核心框架
- [Xpatch](https://github.com/WindySha/Xpatch)：Fork 来源
- [Apkzlib](https://android.googlesource.com/platform/tools/apkzlib)：重打包工具

## 开源协议

LSPatch 基于 **GNU General Public License v3 (GPL-3)** 协议开源 (http://www.gnu.org/copyleft/gpl.html)。
