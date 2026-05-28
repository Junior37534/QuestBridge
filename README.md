<div align="center">
  <img src="src/main/resources/icon.png" width="128" height="128" alt="QuestBridge Logo" />
  <h1>QuestBridge</h1>
  <p><strong>Play Minecraft Java Edition on Meta Quest 3, 3S, or 2 — standalone, with native controllers. No PC. No streaming. No phone.</strong></p>

  <a href="#license">
    <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT" />
  </a>
  <a href="https://github.com/Junior37534/QuestBridge/releases">
    <img src="https://img.shields.io/github/v/release/Junior37534/QuestBridge.svg?color=blue" alt="Release" />
  </a>
  <a href="#requirements">
    <img src="https://img.shields.io/badge/Platform-Meta%20Quest%203%20%2F%203S%20%2F%202-brightgreen.svg" alt="Platform: Meta Quest 3 / 3S / 2" />
  </a>
</div>

---

If you searched for any of these, you're in the right place:

- How to play Minecraft Java Edition on Quest 3 without a PC
- Minecraft Java standalone on Meta Quest 3 / 3S / 2
- Use Quest controllers natively in Minecraft on Android
- Minecraft on Quest 3 with native controller support
- Play Minecraft Java flat panel on Meta Quest headset
- Minecraft Java Edition Quest standalone no PC required
- Meta Quest 3 Minecraft controller input mod

---

QuestBridge is a Minecraft client-side Fabric mod that lets you play **Minecraft Java Edition entirely on your Meta Quest headset** — no PC, no streaming, no latency from a remote connection. It runs the game natively on the headset via [Amethyst-Android](https://github.com/AngelAuraMC/Amethyst-Android), and bridges your Quest controllers to the game using a local WebXR page and a low-latency WebSocket server.

The result: a flat 2D Minecraft panel floating in your mixed reality space, fully playable with your Quest 3 controllers, standalone.

> [!NOTE]
> Developed and tested specifically for **Minecraft 26.1.2** (Fabric, running inside Amethyst-Android). Compatibility with other versions is not guaranteed.

---

<p align="center">
  <video src="https://github.com/user-attachments/assets/b3d941bd-ead1-49a7-99f9-02467f74109c" width="100%" controls>
    Your browser does not support HTML5 video.
  </video>
</p>

---

## Features

- **Truly Standalone**: Minecraft Java Edition running natively on Quest 3, 3S, or 2 — zero PC connection required.
- **Native Quest Controller Input**: WebXR captures your controller inputs and forwards them to the game over a high-speed local WebSocket (port 7374).
- **Immersive Environments**: Switch between native MR Passthrough, solid color backgrounds, custom 360° Skyboxes, and a procedural 3D Space scene with dynamic stars and shooting stars.
- **Controller Haptics**: In-game feedback (breaking blocks, taking damage, eating) sent back to your controllers, with adjustable intensity.
- **Persistent Settings**: Environment and haptic preferences are saved across sessions using IndexedDB and LocalStorage.

---

## Requirements

- **Meta Quest 3** (tested) | **Quest 3S & Quest 2** (supported)
- **Developer Mode** enabled on the headset — see the [Official Meta Developer Guide](https://developer.oculus.com/documentation/native/android/mobile-device-setup/)
- **Seamless Multitasking** enabled:
  1. Open Quest **Settings**
  2. Navigate to **Experimental**
  3. Toggle **Seamless Multitasking** → **ON**

> [!IMPORTANT]
> Without Seamless Multitasking, the browser tab running the input bridge will freeze or terminate when Minecraft comes into focus.

  <p align="center">
    <img src="https://github.com/user-attachments/assets/93bbc0df-a2f2-407d-a402-854b465cf19f" width="600" alt="Enable Seamless Multitasking in Quest Settings" />
  </p>

- **Disable Gestures in Amethyst** (recommended):
  1. Launch Amethyst-Android
  2. Go to **Settings → Control Customizations**

  <p align="center">
    <img src="https://github.com/user-attachments/assets/95897783-abe5-45eb-802b-91f0b846d27a" width="600" alt="Disable Gestures in Amethyst" />
  </p>

- **[Amethyst-Android](https://github.com/AngelAuraMC/Amethyst-Android/releases)** — the Minecraft Java launcher for Android/Quest
- **[Controlify](https://modrinth.com/mod/controlify?version=26.1.2&loader=fabric#download)** and **[Fabric API](https://modrinth.com/mod/fabric-api?version=26.1.2#download)** installed in your mods folder

---

## Installation

1. **Install Amethyst-Android**: Sideload the [Amethyst APK](https://github.com/AngelAuraMC/Amethyst-Android/releases) onto your Quest via SideQuest or ADB.
2. **Launch Vanilla Once**: Open Amethyst, log in, and start vanilla Minecraft. Once the main menu loads, exit.
3. **Create a Fabric Profile**: Inside Amethyst, create a new Fabric profile.
4. **Open the Game Directory**:
   - Select your Fabric profile and tap **Open Game Directory**.
   - This opens the Android file explorer in your active game folder.
   - *Tip:* If no files appear, tap the **three dots** (top-right) and select **Show Hidden Files**.
5. **Install the Mods**:
   - Open the **`mods`** folder (create it if it doesn't exist).
   - Drop `questbridge-x.y.z.jar`, **Controlify**, and **Fabric API** into it.

---

## Usage

<p align="center">
  <img src="https://github.com/user-attachments/assets/08bfc3de-0040-4294-b6f7-e2ee1b656012" width="300" alt="QuestBridge Web UI — Environment selector and Haptic Intensity slider" />
</p>

1. **Launch Minecraft**: Open Amethyst and boot with your Fabric profile.
2. **Open the Quest Browser**: Navigate to:
    > http://localhost:7373
3. **Pick an Environment**: Choose your background (e.g., **Pass** for Mixed Reality Passthrough, **✦ Space** for the 3D cosmos scene).
4. **Enter VR**:
   - Tap **Enter VR**.
   - Accept the immersive session permission prompt.
5. **Position the Minecraft Window**:
   - Press the **Meta button** on the right controller to open your Quest app panel.
   - Drag the Minecraft window into a comfortable position in your space.

> [!NOTE]
> While playing, keep your controller pointer beams aimed slightly away from the Minecraft panel. Pointing directly at the window shifts system focus to it, which pauses the input bridge.
>
> Pointing directly at the panel is still useful for navigating inventories, menus, and settings.

<p align="center">
  <img src="https://github.com/user-attachments/assets/701fdfc8-7b12-48da-b32e-ec60706dc410" width="100%" alt="Minecraft Java running inside an immersive 360 environment on Quest" />
</p>
<p align="center">
  <img src="https://github.com/user-attachments/assets/fcfb368d-794c-4b18-8722-69ce3168aae1" width="100%" alt="Minecraft Java panel in mixed reality passthrough on Quest 3" />
</p>

---

## Amethyst Custom Control Layout (Recommended)

This repository includes a custom control map preset for Amethyst that makes standalone play much smoother — mapping shortcuts for the `ESC` menu, `T` for chat, and other keyboard-dependent actions to controller buttons.

### How to Import

1. Download [questbridge_cmap.json](amethyst/questbridge_cmap.json) and copy it to your Quest's internal storage.
2. Open **Amethyst-Android** on your Quest.
3. Go to **Settings → Control Customizations**.

<p align="center">
  <img src="https://github.com/user-attachments/assets/97ef61c1-347c-4d74-8227-2b1983c347fa" width="600" alt="Step: Click Edit Custom Controls in Amethyst" />
</p>

4. Tap **Edit Custom Controls**.

<p align="center">
  <img src="https://github.com/user-attachments/assets/c1607eed-348c-4ceb-a564-05fe97f6d556" width="600" alt="Step: Click the Gear icon" />
</p>

5. Tap the **Gear icon** at the top-center.
6. Tap **Load** and select `questbridge_cmap.json`.

<p align="center">
  <img src="https://github.com/user-attachments/assets/e9409bed-aea5-4cd7-a838-ffb72aeddd42" width="600" alt="Step: Load questbridge_cmap.json file" />
</p>

---

## Troubleshooting

| Issue | Likely Cause | Fix |
| :--- | :--- | :--- |
| **Inputs not registering** | Seamless Multitasking is off | Enable **Seamless Multitasking** in Quest **Settings → Experimental** |
| **Controllers disconnecting** | Focus shifted to Minecraft panel | Don't aim your pointer beam directly at the Minecraft window while playing |
| **Page not loading** | Mod not running or port conflict | Make sure Minecraft has fully loaded, then navigate to `http://localhost:7373` |
| **Passthrough is black** | Wrong session type initialized | Select the **Pass** card *before* pressing **Enter VR** |
| **No haptic feedback** | Intensity at 0% | Raise the **Haptic Intensity** slider on the Web UI |

---

## How It Works

When Minecraft loads, QuestBridge starts a local HTTP server (port 7373) serving a WebXR page, and a WebSocket server (port 7374) to receive controller data. You open the page in the Quest Browser, enter an immersive session, and the page captures your controller inputs via the WebXR Gamepad API, forwarding them over WebSocket to the mod. The mod then feeds those inputs into Controlify, which Minecraft sees as a standard gamepad.

Haptic feedback works in reverse: the mod detects in-game events and sends haptic commands back through the WebSocket to the browser, which triggers vibration via the WebXR Haptic Actuator API.

---

## Contributing

Contributions are welcome. If you want to improve the WebGL environment, optimize WebSocket packets, or refine controller mappings, feel free to open a Pull Request. For major changes, open an issue first to discuss.

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
