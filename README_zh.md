[English](./README.md) | [Русский](./README_ru.md) | [中文](./README_zh.md)

# PortableRadio 适用于 Minecraft 1.7.10

一个为 Minecraft 1.7.10 设计的模组，添加了功能齐全的对讲机和游戏内语音聊天，专为真实感和性能而打造。

![PortableRadio GUI](https://i.imgur.com/s3TwICQ.png)

## 🌟 功能特性

*   **游戏内语音聊天:** 使用真实的对讲机物品与其他玩家交流。
*   **可调频率:** 在你的对讲机上设置频率，以便在私人频道中交谈。
*   **按键通话 (PTT):** 按住一个键（默认为 `V`）来传输你的声音。
*   **语音激活门限 (智能降噪):** 智能噪音门会自动检测你的声音，减少背景噪音和网络占用。
*   **设备选择:** 直接在游戏内 GUI 中选择你偏好的麦克风和扬声器设备。
*   **音量控制:** 独立调节你的麦克风传输音量和扬声器接收音量。
*   **实时设备测试:** 在游戏中测试你的麦克风和扬声器，以找到完美的设置。
*   **电台音效:** 对语音数据应用音频滤波器，使其听起来像真实的无线电传输。
*   **HUD 指示器:** 屏幕上一个简单、不显眼的图标会显示你的对讲机是开启还是关闭状态。

## 💿 安装

**前置需求:**
*   Minecraft 1.7.10
*   [Minecraft Forge](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.7.10.html) (推荐最新版本)

#### 对于玩家（服务器与客户端）

**重要提示:** 此模组需要在服务器和所有希望连接的客户端上都安装。如果只在一方安装，它将无法工作。

1.  从 [Releases](https://github.com/fuctorial/PortableRadio/releases) 页面下载最新的 `.jar` 文件。
2.  将下载的 `.jar` 文件放入你的 `mods` 文件夹。
3.  启动游戏或服务器。祝你玩得愉快！

## 🎙️ 如何使用

*   **打开界面:** 手持对讲机点击鼠标右键，打开设置菜单。
*   **切换电源:** 按住 `Shift` 并点击鼠标右键，可以打开或关闭对讲机。
*   **开始传输:** 按住“按键通话”键（默认为 `V`）进行讲话。声音将提示您传输的开始和结束。

## 🎨 自定义与资源包

如果你希望为 PortableRadio 创建资源包，请注意以下纹理要求。本模组包含一个验证器来检查纹理尺寸。

*   主 GUI 纹理 (`assets/portableradio/textures/gui/gui_walkie_talkie.png`) **必须为 256x256 像素** 才能正确渲染。

## 🌐 本地化

该模组目前支持以下语言:
*   **英语 (en_US)**
*   **俄语 (ru_RU)**
*   **简体中文 (zh_CN)**

欢迎为其他语言做出贡献！

## 🛠️ 从源码构建

1.  克隆仓库: `git clone https://github.com/fuctorial/PortableRadio.git`
2.  进入项目目录并设置工作区: `gradlew setupDecompWorkspace`
3.  构建项目: `gradlew build`
4.  编译后的 `.jar` 文件将位于 `build/libs/` 目录中。
