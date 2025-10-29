# PortableRadio for Minecraft 1.7.10

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A Minecraft 1.7.10 mod that adds walkie-talkies with fully functional in-game voice chat, designed for realism and performance.

![PortableRadio GUI](https://i.imgur.com/s3TwICQ.png)

## 🌟 Features

*   **In-Game Voice Chat:** Communicate with other players using a realistic walkie-talkie item.
*   **Adjustable Frequency:** Set a frequency on your radio to talk in private channels.
*   **Push-to-Talk (PTT):** Hold down a key (`V` by default) to transmit your voice.
*   **Voice Activation Gate:** A smart noise gate automatically detects your voice, reducing background noise and network usage.
*   **Device Selection:** Choose your preferred microphone and speaker devices directly from the in-game GUI.
*   **Volume Controls:** Independently adjust your microphone transmission volume and speaker reception volume.
*   **Live Device Testing:** Test your microphone and speakers in-game to find the perfect settings.
*   **Radio Sound Effects:** An audio filter is applied to voice data to make it sound like a real radio transmission.
*   **HUD Indicator:** A simple, non-intrusive icon on your screen shows whether your radio is on or off.

## 💿 Installation

**Prerequisites:**
*   Minecraft 1.7.10
*   [Minecraft Forge](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.7.10.html) (Recommended latest version)

#### For Players (Server & Client)

**Important:** This mod is required on both the server and all clients wishing to connect. It will not work if installed only on one side.

1.  Download the latest `.jar` file from the [Releases](https://github.com/fuctorial/PortableRadio/releases) page.
2.  Place the downloaded `.jar` file into your `mods` folder.
3.  Launch the game or server. Enjoy!

## 🎙️ How to Use

*   **Open GUI:** Right-click with the Walkie-Talkie in hand to open the settings menu.
*   **Toggle Power:** Sneak (hold `Shift`) and right-click to turn the radio on or off.
*   **Transmit:** Hold down the Push-to-Talk key (default `V`) to speak. A sound will play to indicate the start and end of your transmission.

## 🎨 Customization & Resource Packs

If you wish to create a resource pack for PortableRadio, please note the following texture requirements. The mod includes a validator to check texture sizes.

*   The main GUI texture (`assets/portableradio/textures/gui/gui_walkie_talkie.png`) **must be 256x256 pixels** to render correctly.

## 🌐 Localization

The mod currently includes support for the following languages:
*   **English (en_US)**
*   **Russian (ru_RU)**

Contributions for other languages are welcome!

## 🛠️ Building from Source

1.  Clone the repository: `git clone https://github.com/fuctorial/PortableRadio.git`
2.  Navigate to the project directory and set up the workspace: `gradlew setupDecompWorkspace`
3.  Build the project: `gradlew build`
4.  The compiled `.jar` file will be located in the `build/libs/` directory.