# QuestBridge

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](#license)
[![Release](https://img.shields.io/github/v/release/junior/QuestBridge.svg?color=blue)](https://github.com/junior/QuestBridge/releases)
[![Platform: Meta Quest 3](https://img.shields.io/badge/Platform-Meta%20Quest%203-brightgreen.svg)](#requirements)

QuestBridge is a Minecraft client-side Fabric mod that bridges Meta Quest 3 controllers directly to Minecraft Java Edition running natively on the headset. It hosts a local WebXR webpage that captures controller inputs and sends them to the running game instance via a low-latency WebSocket server, enabling a console-like immersive experience entirely standalone without a PC.

---

## Demo

<!-- VIDEO: Immersive gameplay demo showing the WebXR background rendering (Space/Passthrough) alongside Minecraft running as a 3D panel with smooth controller tracking -->

---

## Features

- **Standalone Quest 3 Support**: Play Minecraft Java Edition in VR/MR directly on the headset with zero PC connection required.
- **Low-Latency Controller Mapping**: WebXR-based controller capture forwarded over a high-speed local WebSocket (port 7374).
- **Premium Immersive Environments**: Smooth switching between native MR Passthrough, solid backgrounds, custom 360° Skyboxes, and a high-fidelity procedural 3D Space background with dynamic stars and shooting stars.
- **Integrated Controller Haptics**: Native in-game action feedback (breaking blocks, taking damage, eating) transmitted back to controllers with customizable intensity.
- **Persistent Settings**: Saves environment preferences and haptic options across sessions using IndexedDB and LocalStorage.

---

## Requirements

Ensure all of the following requirements are met before starting:

- **Meta Quest 3** (optimized and tested for Quest 3).
- **Developer Mode Enabled** on the headset. Refer to the [Official Meta Developer Guide](https://developer.oculus.com/documentation/native/android/mobile-device-setup/) for setup steps.
- **"Seamless Multitasking"** enabled in the headset:
  1. Open Quest Settings.
  2. Navigate to **Experimental**.
  3. Toggle **Seamless Multitasking** to **ON**.
  > [!IMPORTANT]
  > Without Seamless Multitasking enabled, the browser background will freeze or terminate when Minecraft comes into focus.
- **Minecraft: Java Edition** running via the **Amethyst-Android** launcher:
  1. Download the latest APK from the [Amethyst Releases Page](https://github.com/AngelAuraMC/Amethyst-Android).
  2. Log in using your Microsoft Account.
  3. Launch vanilla Minecraft once to generate required engine files.
  4. Create and configure a **Fabric** profile.
- **Controlify** mod installed in your Fabric profile mods directory.
- **Fabric API** installed in your Fabric profile mods directory.

---

## Installation

1. **Install Amethyst-Android**: Install the Amethyst APK on your Meta Quest 3 via SideQuest or ADB.
2. **Launch Vanilla Once**: Open Amethyst, log in, and start vanilla Minecraft. Once the main menu loads, exit the game.
3. **Configure Fabric Profile**: Inside Amethyst, create a new Fabric profile.
4. **Access the Game Directory**:
   - Open your favorite Android File Manager (e.g., Amaze, CX File Explorer) on the Quest.
   - Navigate to `/sdcard/Android/data/org.angelauramc.amethyst/files/.minecraft/`.
   - Ensure "Show Hidden Files" is enabled in your file manager settings.
5. **Install Mods**:
   - Download the latest `questbridge-x.y.z.jar` from the [Releases](#) tab of this repository.
   - Place the JAR into the `/sdcard/Android/data/org.angelauramc.amethyst/files/.minecraft/mods/` directory alongside `Controlify` and the `Fabric API`.

---

## Usage

<!-- SCREENSHOT: WebXR controller.html UI interface on the Meta Quest Browser showing the Environment Grid and Haptic Slider -->

1. **Launch Minecraft**: Open Amethyst and boot the game using your configured Fabric profile.
2. **Launch Quest Browser**: Open the native Meta Quest Browser and navigate to:
   ```text
   http://localhost:7373
   ```
3. **Select Environment**: Pick your preferred theme on the Web UI (e.g., **Pass** for Mixed Reality Passthrough, **✦ Space** for the custom 3D cosmos background).
4. **Enter VR**:
   - Click the **Enter VR** button on the page.
   - The browser will ask for immersive permissions. Accept them.
5. **Position Minecraft**:
   - Press the **Meta button** on the right controller to bring up your Quest 2D app panel.
   - Drag the Minecraft window into a comfortable 3D space in front of you.
6. **Controller Positioning / Avoid Pointer Lock**:
   > [!IMPORTANT]
   > **Do not point your controller beams directly at the Minecraft window while playing.** 
   > Doing so will shift the headset's focus away from the QuestBridge browser tab and break input capture. Keep your controllers aimed slightly downward or away from the panel while executing actions.
   > 
   > *Note: Pointing directly at the panel remains useful when navigating Minecraft’s inventories, chat, or game settings.*

<!-- SCREENSHOT: Final setup showcasing the immersive WebGL space environment with the Minecraft Java panel correctly positioned -->

---

## Troubleshooting

| Issue | Potential Cause | Resolution |
| :--- | :--- | :--- |
| **Inputs not registering** | Seamless Multitasking is disabled | Ensure **Seamless Multitasking** is turned ON in the Quest Experimental settings. |
| **Controllers disconnected** | Focus shift | Confirm that you are not pointing the controllers directly at the Minecraft panel. |
| **Page not loading** | Server port conflict or mod not running | Verify Minecraft has fully loaded Amethyst and ensure the browser is pointed to `http://localhost:7373`. |
| **Passthrough mode is black** | Permission block or session type issue | Ensure you selected the **Pass** card *before* pressing **Enter VR** so that the system correctly initializes an `immersive-ar` session. |
| **Haptics missing** | Intensity set to 0% | Adjust the **Haptic Intensity** slider to a value greater than 0% on the Web UI. |

---

## Contributing

Contributions are welcome! If you want to improve the WebGL environment rendering, optimize WebSocket packets, or refine gamepad remappings, feel free to open a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

---

## License

<!-- TODO: Clarify which license you would like to apply to the repository. The default recommendation is MIT. -->

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
