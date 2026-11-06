# CurseForge release sheet

This file records the CurseForge project settings and uploaded release filenames. The listing body starts after the horizontal rule. It matches `MODRINTH.md` except that mod links stay on CurseForge. Gallery images, titles and descriptions come from the Gallery table in `MODRINTH.md`.

## Project settings

| Field | Value |
| --- | --- |
| Name | Cinematic Zoom Extended |
| Slug | `cinematic-zoom-extended` |
| Project ID | `1763623` |
| Class | Mods |
| Summary | Configurable zoom with two profiles, animated cinema bars, easing curves, ultra-wide zoom, snap targets, and HUD controls. |
| License | MIT |
| Environment | Client-side only |
| Loaders | Fabric, Quilt, Forge, NeoForge |
| Avatar | `src/main/resources/assets/cinematiczoom/icon.png` |
| Source | https://github.com/simply-sunny/cinematic-zoom-extended |
| Issues | https://github.com/simply-sunny/cinematic-zoom-extended/issues |
| Wiki / docs | https://github.com/simply-sunny/cinematic-zoom-extended#configure |
| Required dependencies | Fabric: Fabric API (`fabric-api`), Cloth Config (`cloth-config`), Mod Menu (`modmenu`). NeoForge and Forge 1.20.1: Cloth Config. Forge 1.21.1 and newer: none. |

## Version metadata

The published 1.0.0 files retain these filenames. File IDs are not recorded in the repository; read them from the existing project before publishing another version.

| JAR | Minecraft | Java |
| --- | --- | --- |
| `cinematiczoom-extended-1.20.1-1.0.0.jar` | 1.20.1 | 17 |
| `cinematiczoom-extended-1.21.1-1.0.0.jar` | 1.21.1 | 21 |
| `cinematiczoom-extended-1.21.2-1.21.4-1.0.0.jar` | 1.21.2–1.21.4 | 21 |
| `cinematiczoom-extended-1.21.5-1.21.6-1.0.0.jar` | 1.21.5–1.21.6 | 21 |
| `cinematiczoom-extended-1.21.7-1.21.8-1.0.0.jar` | 1.21.7–1.21.8 | 21 |
| `cinematiczoom-extended-1.21.9-1.21.11-1.0.0.jar` | 1.21.9–1.21.11 | 21 |
| `cinematiczoom-extended-26.1-1.0.0.jar` | 26.1, 26.1.1, 26.1.2 | 25 |
| `cinematiczoom-extended-26.2-1.0.0.jar` | 26.2 | 25 |
| `cinematiczoom-extended-26.3-1.0.0.jar` | 26.3 | 25 |

1.1.0 and newer use `cinematic-zoom-extended-<jar>-<loader>-<version>.jar`, one file per jar. Fabric jars carry the Quilt tag and the same Minecraft versions as the table in `MODRINTH.md`. Forge and NeoForge jars carry their exact Minecraft version. The upload script reads the version and prefix from `gradle.properties` and the notes from `CHANGELOG.md`. Bump `mod_version` and add release notes before publishing changed jars.

The uploaded 1.1.0 files and their file IDs:

| JAR | File ID |
| --- | --- |
| `cinematic-zoom-extended-1.20.1-fabric-1.1.0.jar` | `9439030` |
| `cinematic-zoom-extended-1.20.1-neoforge-1.1.0.jar` | `9439031` |
| `cinematic-zoom-extended-1.20.1-forge-1.1.0.jar` | `9439032` |
| `cinematic-zoom-extended-1.21.1-fabric-1.1.0.jar` | `9439033` |
| `cinematic-zoom-extended-1.21.1-neoforge-1.1.0.jar` | `9439034` |
| `cinematic-zoom-extended-1.21.1-forge-1.1.0.jar` | `9439042` |
| `cinematic-zoom-extended-1.21.2-1.21.4-fabric-1.1.0.jar` | `9439043` |
| `cinematic-zoom-extended-1.21.2-neoforge-1.1.0.jar` | `9439044` |
| `cinematic-zoom-extended-1.21.5-1.21.6-fabric-1.1.0.jar` | `9439045` |
| `cinematic-zoom-extended-1.21.5-neoforge-1.1.0.jar` | `9439046` |
| `cinematic-zoom-extended-1.21.5-forge-1.1.0.jar` | `9439047` |
| `cinematic-zoom-extended-1.21.7-1.21.8-fabric-1.1.0.jar` | `9439048` |
| `cinematic-zoom-extended-1.21.7-neoforge-1.1.0.jar` | `9439049` |
| `cinematic-zoom-extended-1.21.7-forge-1.1.0.jar` | `9439050` |
| `cinematic-zoom-extended-1.21.9-1.21.11-fabric-1.1.0.jar` | `9439053` |
| `cinematic-zoom-extended-1.21.11-neoforge-1.1.0.jar` | `9439054` |
| `cinematic-zoom-extended-1.21.11-forge-1.1.0.jar` | `9439055` |
| `cinematic-zoom-extended-26.1-fabric-1.1.0.jar` | `9439056` |
| `cinematic-zoom-extended-26.1-neoforge-1.1.0.jar` | `9439057` |
| `cinematic-zoom-extended-26.1-forge-1.1.0.jar` | `9439058` |
| `cinematic-zoom-extended-26.2-fabric-1.1.0.jar` | `9439059` |
| `cinematic-zoom-extended-26.2-neoforge-1.1.0.jar` | `9439060` |
| `cinematic-zoom-extended-26.2-forge-1.1.0.jar` | `9439061` |
| `cinematic-zoom-extended-26.3-fabric-1.1.0.jar` | `9439062` |
| `cinematic-zoom-extended-26.3-neoforge-1.1.0.jar` | `9439063` |
| `cinematic-zoom-extended-26.3-forge-1.1.0.jar` | `9439065` |

```bash
./gradlew test buildAll
for mc in 1.21.1 1.21.5 1.21.7 1.21.11 26.1 26.2 26.3; do ./gradlew -p platform/forge build -Pmc=$mc; done
for mc in 1.21.1 1.21.2 1.21.5 1.21.7 1.21.11 26.1 26.2 26.3; do ./gradlew -p platform/neoforge build -Pmc=$mc; done
./gradlew -p platform/forge-1.20.1 build
./gradlew -p platform/forge-1.20.1 clean build -PlegacyNeo
python3 scripts/publish-curseforge.py 1763623 --dry-run
```

Set `CURSEFORGE_API_KEY` in the environment for the dry run or upload. The dry run reads CurseForge version tags and prints upload metadata. Remove `--dry-run` only when publishing a new release.

---

Smooth, configurable zoom with animated cinema bars.

Hold a key and the view eases in while black bars slide across the screen, then eases back out when you let go. Cinematic Zoom Extended adds two independent profiles, six easing curves, ultra-wide zoom, snap targets and fine control over what the HUD hides.

A fork of [Cinematic Zoom (Fabric)](https://www.curseforge.com/minecraft/mc-mods/cinematic-zoom-fabric) by [Mel1x](https://github.com/mel1x), based on the original [Cinematic Zoom](https://www.curseforge.com/minecraft/mc-mods/cinematic-zoom) by [KubePixel](https://www.curseforge.com/members/kubepixel/projects). Published with permission. Not affiliated with or endorsed by either author.

## Features

- Two profiles. Cinematic (with bars) and Regular (without), each with its own key, zoom, timing, curves, toggle mode, snap targets and HUD options.
- Smooth motion. Per-frame easing with six curves and separate in and out durations. Bars have their own Instant, Linear, or Smooth animation.
- High magnification. Zoom accurately up to 500×.
- Aspect ratios. Frame the view at 2.39:1, 2.35:1, 2:1, 1.85:1, 16:9, 16:10, 4:3, 1:1, or your own ratio. Wide ratios add top and bottom bars, and narrow ratios add side bars. Switch with the left and right arrow keys.
- Framing. Adjust the bars live with the arrow keys, zoom out to 0.5x with ultra-wide, step the wheel in fixed increments, and snap to targets like `2x` or `45fov`.
- Camera and HUD. Scale mouse sensitivity, remove view bobbing, and hide the HUD, crosshair and hands independently. F3 hides with the HUD.
- Settings screen. Cloth Config on Fabric, NeoForge and Forge 1.20.1. A built-in screen on newer Forge.
- Easy switching. Cinematic Zoom 1.3 configs migrate automatically, and Zoomify, Ok Zoomer, Just Zoom and similar mods are imported on first launch.

## Installation

1. Install your loader and these mods for your Minecraft version:
   - Fabric or Quilt: Fabric API, Cloth Config and Mod Menu.
   - NeoForge and Forge 1.20.1: Cloth Config.
   - Forge 1.21.1 and newer: nothing else.
2. Put the jar for your Minecraft version and loader in `mods`.
3. Remove any other Cinematic Zoom build; only one can be installed.

## Usage

| Action | Default key |
| --- | --- |
| Cinematic zoom | `C` |
| Zoom without bars | `V` |
| Previous / next aspect ratio | `←` / `→` |
| Increase bar size | `↑` |
| Decrease bar size | `↓` |

Hold the zoom key to zoom and release to return, or turn on **Toggle zoom**. Scroll the mouse wheel while zooming to change magnification. Settings are under **Mod Menu → Cinematic Zoom Extended**, or your loader's mod list on Forge and NeoForge.

## Compatibility

- **Fabric and Quilt:** 1.20.1, 1.21.1 to 1.21.11, 26.1 to 26.1.2, 26.2 and 26.3.
- **NeoForge:** 1.20.1, 1.21.1, 1.21.2, 1.21.5, 1.21.7, 1.21.11, 26.1, 26.2 and 26.3.
- **Forge:** the same versions as NeoForge, except 1.21.2.
- **Java:** 17 for 1.20.1, 21 for 1.21.x, 25 for 26.x.
- Client-side only. Servers do not need it.
- Other zoom mods conflict with FOV handling. Import their settings instead of running both.
- **Sodium, Iris and shaders:** no known conflicts, not specifically tested.
- Quilt runs the Fabric jar with Fabric API. OptiFine is unsupported.

Full configuration, import and troubleshooting notes are on [GitHub](https://github.com/simply-sunny/cinematic-zoom-extended#readme).

## Credits and license

Thanks to KubePixel for the original concept, Mel1x for the Fabric implementation, and ColoredChromium for the velvet curtain texture in the icon ([CC BY 3.0](https://creativecommons.org/licenses/by/3.0/), [source](https://www.deviantart.com/coloredchromium/art/Curtains-Red-Transparency-Light-424984584)). Mod code and original assets are MIT, see the [LICENSE](https://github.com/simply-sunny/cinematic-zoom-extended/blob/main/LICENSE). The icon texture remains CC BY 3.0. Minecraft is a trademark of Mojang Studios.
