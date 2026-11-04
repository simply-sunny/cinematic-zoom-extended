<p align="center"><img src="src/main/resources/assets/cinematiczoom/icon.png" width="128" alt="Cinematic Zoom Extended icon"></p>

<h1 align="center">Cinematic Zoom Extended</h1>

<p align="center">A configurable zoom with animated cinema bars.<br>Fabric, Forge &amp; NeoForge &nbsp;|&nbsp; 1.20.1 – 26.3</p>

<p align="center">
<a href="https://modrinth.com/mod/cinematic-zoom-extended"><img src="https://img.shields.io/modrinth/dt/cinematic-zoom-extended?logo=modrinth&label=Modrinth&color=1bd96a" alt="Modrinth downloads"></a>
<a href="https://www.curseforge.com/minecraft/mc-mods/cinematic-zoom-extended"><img src="https://img.shields.io/curseforge/dt/1763623?logo=curseforge&label=CurseForge&color=f16436" alt="CurseForge downloads"></a>
<a href="https://github.com/simply-sunny/cinematic-zoom-extended/releases"><img src="https://img.shields.io/github/v/release/simply-sunny/cinematic-zoom-extended?label=release&color=2ea44f" alt="Latest release"></a>
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

Choose the jar for your Minecraft version and loader:

| Loader | Minecraft |
| --- | --- |
| Fabric, Quilt | 1.20.1, 1.21.1, 1.21.2–1.21.4, 1.21.5–1.21.6, 1.21.7–1.21.8, 1.21.9–1.21.11, 26.1–26.1.2, 26.2, 26.3 |
| NeoForge | 1.20.1, 1.21.1, 1.21.2, 1.21.5, 1.21.7, 1.21.11, 26.1, 26.2, 26.3 |
| Forge | 1.20.1, 1.21.1, 1.21.5, 1.21.7, 1.21.11, 26.1, 26.2, 26.3 |

Forge published no 1.21.2 loader.

Fabric needs Fabric API, Cloth Config, and Mod Menu. NeoForge needs its matching Cloth Config distribution. Forge 1.20.1 needs Cloth Config for Forge; newer Forge uses a built-in settings screen and needs neither Cloth Config nor Mod Menu. Open settings through your loader's mod list.

Java requirements: 17 for 1.20.1, 21 for 1.21.x, and 25 for 26.x. Quilt runs the Fabric jar. See [verification](docs/VERIFICATION.md) for results and limits.

New builds use `cinematic-zoom-extended-<jar>-<loader>-<version>.jar`. The published 1.0.0 downloads keep their original `cinematiczoom-extended-<jar>-1.0.0.jar` filenames.

## Use

| Action | Default key |
| --- | --- |
| Cinematic Zoom (hold or toggle) | `C` |
| Zoom without bars | `V` |
| Previous / next aspect profile | `←` / `→` |
| Increase bar size | `↑` |
| Decrease bar size | `↓` |

All keys are rebindable under **Options → Controls → Key Binds → Cinematic Zoom**.

- Hold the zoom key to zoom in. Release it to return. Enable **Toggle zoom** to press once to turn it on and once to turn it off.
- Use the mouse wheel while zooming to change magnification.
- With **Ultra-wide zoom** enabled, scroll back past 1× to reach 0.5×.
- Zoom and overlays are disabled while a GUI screen is open.

## Configure

Open **Mod Menu → Cinematic Zoom Extended → Settings**. Every setting is there, including both profiles, curves, timings, HUD hiding, snap targets, and wheel behavior.

| Setting | Default | Notes |
| --- | --- | --- |
| Starting zoom | `3×` in both profiles | Each profile keeps its own magnification. |
| Bar height | `15%` | Cinematic profile only; arrow keys adjust it by `1%`. |
| Zoom duration | `240 ms` in and out | Separate values for each profile. |
| Easing curve | Exponential | Separate curves for zoom in and out. |
| Hide HUD, crosshair, hands | On | Each option is independent in each profile. |
| Toggle zoom | Off | Hold the key by default. |
| Mouse wheel during zoom | On | Changes magnification while zooming. |
| Ultra-wide zoom | Off | Allows zooming out to `0.5×` when enabled. |
| Snap targets | Empty | Set targets such as `2x` or `45fov` per profile. |

Settings are saved to `config/cinematiczoom.json` in your Minecraft folder:

| OS | Path |
| --- | --- |
| Windows | `%appdata%\.minecraft\config\cinematiczoom.json` |
| macOS | `~/Library/Application Support/minecraft/config/cinematiczoom.json` |
| Linux | `~/.minecraft/config/cinematiczoom.json` |

New installations default to accurate magnification up to **500×** and independent **Linear** bar animation. The aspect list includes Auto, 2.39:1, 2.35:1, 2:1, 1.85:1, 16:9, 16:10, 4:3, and 1:1. Add custom ratios or remove/reorder entries to control cycling. Ratios wider than the window add top/bottom bars; narrower ratios add side bars. This crops the composition with an overlay; it does not change Minecraft's viewport or projection aspect.

Left/right cycling works while cinematic zoom is active. Enable global cycling to select a profile outside zoom. Selection is saved. Entry, exit, and profile changes each have their own Instant, Linear, or Smooth mode and duration. Profile changes leave magnification unchanged. Regular zoom always clears bars.

Existing configurations retain legacy FOV and percentage bars and their saved zoom limits. Migration creates `cinematiczoom.json.v1.bak`. Invalid files receive a separate backup; failed backups disable writes to preserve the original. Unknown settings survive saves. Delete the config to start with the new defaults.

Both settings screens expose the maximum magnification, wheel settings, custom ratios, and all profile options. The Forge native editor uses comma-separated ratios in cycling order and validates entries when saving.

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

See [multi-loader development](docs/DEVELOPMENT.md) for native builds, pinned dependencies, and real-client tests.

Requires JDK 25. 1.20.1 compiles with `--release 17`, and 1.21.x with 21.

```bash
./gradlew clean test buildAll
```

This runs the headless regression suite and builds every target. Jars land in `build/libs/`. To build one target:

```bash
./gradlew -PonlyTarget=1.21.7 :1.21.7:build
```

To add a Minecraft target, add it to `minecraftTargets` in `build.gradle` and `targets` in `settings.gradle`, then put its version-specific code under `versions/<mc>/`. Shared code stays in `src/common/java`; the existing legacy and modern source sets handle API differences across targets.

The repository and new artifact prefix are `cinematic-zoom-extended`. The mod ID stays `cinematiczoom_extended`, the Java package and asset namespace stay `mix.cinematiczoom` and `cinematiczoom`, and settings stay in `config/cinematiczoom.json`. Existing key binds and settings carry over without migration. The 1.0.0 tag and published downloads keep their original names and contents.

Release settings and listing copy live in [MODRINTH.md](MODRINTH.md) and [CURSEFORGE.md](CURSEFORGE.md).

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
- **Sodium, Iris, and shaders:** no known conflicts, but not specifically tested.
- **Quilt:** tested with the actual loader on every Fabric version. See the [verification report](docs/VERIFICATION.md).
- **Forge and NeoForge:** each jar loads only on its exact Minecraft version.
- **OptiFine:** unsupported.

## License

MIT, see [LICENSE](LICENSE). Original work copyright © 2025 Melix. Changes copyright © 2026 Saunak Karnati (simply-sunny).

Credits: KubePixel (original concept and Forge mod), Mel1x (upstream Fabric implementation and the 1.3 base), simply-sunny (Extended features, profiles, tests, and releases).

The icon uses a velvet curtain texture by [ColoredChromium](https://www.deviantart.com/coloredchromium/art/Curtains-Red-Transparency-Light-424984584), licensed under [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/). Minecraft is a trademark of Mojang Studios.
