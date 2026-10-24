<p align="center"><img src="src/main/resources/assets/cinematiczoom/icon.png" width="128" alt="Cinematic Zoom Extended icon"></p>

<h1 align="center">Cinematic Zoom Extended</h1>

<p align="center">A configurable zoom with animated cinema bars.<br>Fabric &amp; Quilt &nbsp;|&nbsp; 1.20.1 – 26.3</p>

<p align="center">
<a href="https://modrinth.com/mod/cinematic-zoom-extended"><img src="https://img.shields.io/modrinth/dt/cinematic-zoom-extended?logo=modrinth&label=Modrinth&color=1bd96a" alt="Modrinth downloads"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/cinematic-zoom-extended"><img src="https://img.shields.io/curseforge/dt/1763623?logo=curseforge&label=CurseForge&color=f16436" alt="CurseForge downloads"></a>
<a href="https://github.com/simply-sunny/CinematicZoom-Extended/releases"><img src="https://img.shields.io/github/v/release/simply-sunny/CinematicZoom-Extended?label=release&color=2ea44f" alt="Latest release"></a>
<a href="LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="MIT license"></a>
</p>

---

Hold a key and the view eases in while black bars slide across the screen. Release the key and it eases back out. Two independent profiles, six easing curves, ultra-wide zoom, and snap targets like `2x` or `45fov`. It also scales mouse sensitivity, removes view bobbing, and hides the HUD, crosshair, and hands independently.

> A fork of [Cinematic Zoom (Fabric)](https://modrinth.com/mod/cinematic-zoom-fabric) by [Mel1x](https://github.com/mel1x), based on the original [Cinematic Zoom](https://www.curseforge.com/minecraft/mc-mods/cinematic-zoom) by [KubePixel](https://www.curseforge.com/members/kubepixel/projects). Published with Mel1x's permission ([PR #8](https://github.com/mel1x/CinematicZoom/pull/8#issuecomment-6139411837)). Not affiliated with or endorsed by either author.

## Install

Client-side only. Servers don't need it.

1. Install [Fabric Loader](https://fabricmc.net/use/) (or Quilt Loader), [Fabric API](https://modrinth.com/mod/fabric-api), [Cloth Config](https://modrinth.com/mod/cloth-config), and [Mod Menu](https://modrinth.com/mod/modmenu) for your Minecraft version. Cloth Config and Mod Menu are required.
2. Put the jar for your Minecraft version in `mods`.
3. Remove any other Cinematic Zoom build. Only one can be installed.

| Minecraft | Jar | Java | Cloth Config |
| --- | --- | --- | --- |
| 1.20.1 | `1.20.1` | 17+ | 11.1.136+ |
| 1.21.1 | `1.21.1` | 21+ | 15.0.140+ |
| 1.21.2–1.21.4 | `1.21.2-1.21.4` | 21+ | 16.0.143+ |
| 1.21.5–1.21.6 | `1.21.5-1.21.6` | 21+ | 18.0.145+ |
| 1.21.7–1.21.8 | `1.21.7-1.21.8` | 21+ | 19.0.147+ |
| 1.21.9–1.21.11 | `1.21.9-1.21.11` | 21+ | 20.0.149+ |
| 26.1–26.1.2 | `26.1` | 25+ | 26.1.154+ |
| 26.2–26.2.2 | `26.2` | 25+ | 26.2.155+ |
| 26.3 | `26.3` | 25+ | 26.3.159+ |

Jars are named `cinematiczoom-extended-<jar>-<version>.jar`.

## Use

| Action | Default key |
| --- | --- |
| Cinematic Zoom (hold or toggle) | `C` |
| Zoom without bars | Unbound. Bind it in Controls. |
| Increase bar size | `↑` |
| Decrease bar size | `↓` |

All keys are rebindable under **Options → Controls → Key Binds → Cinematic Zoom**.

- Hold the zoom key to zoom in. Release it to return. Enable **Toggle zoom** to press once to turn it on and once to turn it off.
- Use the mouse wheel while zooming to change magnification.
- With **Ultra-wide zoom** enabled, scroll back past 1× to reach 0.5×.
- Zoom and overlays are disabled while a GUI screen is open.

## Configure

Open **Mod Menu → Cinematic Zoom Extended → Settings**. Every setting is there, including both profiles, curves, timings, HUD hiding, snap targets, and wheel behavior.

Settings are saved to `config/cinematiczoom.json` in your Minecraft folder:

- **Windows:** `%appdata%\.minecraft\config\cinematiczoom.json`
- **macOS:** `~/Library/Application Support/minecraft/config/cinematiczoom.json`
- **Linux:** `~/.minecraft/config/cinematiczoom.json`

Delete the file to reset to defaults. Two keys exist only in the JSON. Edit them, then restart:

| Key | Default | Notes |
| --- | --- | --- |
| `wheelStep` | `0.05` | Wheel sensitivity, 0.01–0.25. |
| `minZoomMultiplier` | `0.0333` (1/30) | Lower allows more zoom. The maximum factor is `1 / value`. |

### Coming from another zoom mod

If `config/cinematiczoom.json` is missing, Extended imports what it recognizes on first launch:

- **Zoomify:** `config/zoomify.json`
- **Ok Zoomer:** `config/ok_zoomer/config.toml` or legacy `okzoomer.json`
- **Just Zoom:** `config/justzoom/config.json`
- **Other zoom mods:** any JSON or TOML file with `zoom` in its path, matched by key name.

WI Zoom and Logical Zoom keep no config file. An existing `cinematiczoom.json` is never overwritten. Delete it and relaunch to re-import.

### Migrating from Cinematic Zoom 1.3

Extended replaces the original `cinematiczoom-*.jar`. Remove that jar and install the Extended one. Extended has a new mod ID but keeps the config filename, so leave `config/cinematiczoom.json` in place. These legacy keys migrate on first launch: `startingZoom`, `baseZoomMultiplier`, `barsPercent`, `smoothMs`, `hideHudDuringZoom`, `enableCinematicCamera`, `toggleMode`, `minZoomMultiplier`. To start fresh, delete the file and relaunch.

## Develop

Requires JDK 25. 1.20.1 compiles with `--release 17`, and 1.21.x with 21.

```bash
./gradlew clean test buildAll
```

This runs the headless regression suite and builds every target. Jars land in `build/libs/`. To build one target:

```bash
./gradlew :1.21.7:build
```

## Troubleshooting

| Problem | Fix |
| --- | --- |
| `requires cloth-config2` | Install Cloth Config for your Minecraft version. |
| `requires modmenu` | Install Mod Menu for your Minecraft version. |
| `incompatible with cinematiczoom` | Keep only one Cinematic Zoom jar in `mods`. |
| Crash on launch | Check Java: 17, 21, or 25, depending on Minecraft. |
| Settings screen missing | Install both Mod Menu and Cloth Config. |
| Zoom key does nothing | Bind **Cinematic Zoom** under Controls. |
| Wheel does not zoom | Enable **Mouse wheel during zoom** and hold the zoom key. |
| Bars do not appear | Use the cinematic key and set the bar height above 0. |
| Broken config | Delete `config/cinematiczoom.json` and relaunch. |

Logs are in `.minecraft/logs/latest.log`. Include the log and your Minecraft, mod, and loader versions when you report an issue.

## Limits

- **Original Cinematic Zoom:** not compatible. Only one build can be installed.
- **Other zoom mods:** may conflict with FOV handling. Don't run them together. Import their settings instead.
- **Sodium, Iris, and shaders:** no known conflicts, but not specifically tested in 1.0.0.
- **Quilt:** supported. Quilt Loader runs the Fabric jars, with Fabric API installed as on Fabric.
- **OptiFine:** unsupported.

## License

MIT, see [LICENSE](LICENSE). Original work copyright © 2025 Melix. Changes copyright © 2026 Saunak Karnati (simply-sunny).

Credits: KubePixel (original concept and Forge mod), Mel1x (upstream Fabric implementation and the 1.3 base), simply-sunny (Extended features, profiles, tests, and releases).

The icon uses a velvet curtain texture by [ColoredChromium](https://www.deviantart.com/coloredchromium/art/Curtains-Red-Transparency-Light-424984584), licensed under [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/). Minecraft is a trademark of Mojang Studios.
