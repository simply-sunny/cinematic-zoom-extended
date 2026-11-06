# Modrinth release sheet

This file is the source of truth for the Modrinth project settings, version metadata, and listing body. The listing body starts after the horizontal rule.

## Project settings

| Field | Value |
| --- | --- |
| Title | **Cinematic Zoom Extended** |
| Slug | `cinematic-zoom-extended` |
| Project type | Mod |
| Project ID | `nGcifVtU` |
| Status | In review on Modrinth as of 2026-11-04 (`processing`); retain the existing project and uploaded versions. |
| CurseForge project ID | `1763623` (slug `cinematic-zoom-extended`), uploaded with `scripts/publish-curseforge.py` |
| Summary | Configurable zoom with two profiles, animated cinema bars, easing curves, ultra-wide zoom, snap targets, and HUD controls. Fabric, Quilt, Forge and NeoForge 1.20.1–26.3, client-side. |
| License | MIT |
| Environment | Client-side only (`client_only`) |
| Featured categories | `utility`, `game-mechanics` |
| Additional categories | `decoration` |
| Source | https://github.com/simply-sunny/cinematic-zoom-extended |
| Issues | https://github.com/simply-sunny/cinematic-zoom-extended/issues |
| Wiki / docs | https://github.com/simply-sunny/cinematic-zoom-extended#configure |
| Discord | *(none)* |
| Donation links | *(none)* |
| Icon | `src/main/resources/assets/cinematiczoom/icon.png` — 512×512 PNG |
| Featured gallery image | 1280×720 screenshot |

## Version metadata

Version 1.1.0 uses channel `release` and one version per jar. Fabric jars list loaders `fabric` and `quilt` and the versions below. Forge and NeoForge jars list `forge` or `neoforge` and their exact Minecraft version: 1.20.1, 1.21.1, 1.21.2 (NeoForge only), 1.21.5, 1.21.7, 1.21.11, 26.1, 26.2, or 26.3. Filenames are `cinematic-zoom-extended-<jar>-<loader>-1.1.0.jar`. Version names follow `Cinematic Zoom Extended 1.1.0 (1.21.2–1.21.4, Fabric)`.

| Jar | Fabric and Quilt `game_versions` |
| --- | --- |
| `1.20.1` | `1.20.1` |
| `1.21.1` | `1.21.1` |
| `1.21.2-1.21.4` | `1.21.2`, `1.21.3`, `1.21.4` |
| `1.21.5-1.21.6` | `1.21.5`, `1.21.6` |
| `1.21.7-1.21.8` | `1.21.7`, `1.21.8` |
| `1.21.9-1.21.11` | `1.21.9`, `1.21.10`, `1.21.11` |
| `26.1` | `26.1`, `26.1.1`, `26.1.2` |
| `26.2` | `26.2` |
| `26.3` | `26.3` |

The uploaded 1.1.0 files and their version IDs:

| JAR | Version ID |
| --- | --- |
| `cinematic-zoom-extended-1.20.1-fabric-1.1.0.jar` | `TzxorGxh` |
| `cinematic-zoom-extended-1.20.1-neoforge-1.1.0.jar` | `dNZABjZC` |
| `cinematic-zoom-extended-1.20.1-forge-1.1.0.jar` | `xqp5N00l` |
| `cinematic-zoom-extended-1.21.1-fabric-1.1.0.jar` | `EsyNbueu` |
| `cinematic-zoom-extended-1.21.1-neoforge-1.1.0.jar` | `ajf88M1m` |
| `cinematic-zoom-extended-1.21.1-forge-1.1.0.jar` | `BtHgzjAT` |
| `cinematic-zoom-extended-1.21.2-1.21.4-fabric-1.1.0.jar` | `RemDYNEQ` |
| `cinematic-zoom-extended-1.21.2-neoforge-1.1.0.jar` | `o8F3bIQz` |
| `cinematic-zoom-extended-1.21.5-1.21.6-fabric-1.1.0.jar` | `kQTeZSC2` |
| `cinematic-zoom-extended-1.21.5-neoforge-1.1.0.jar` | `dNlri6ZO` |
| `cinematic-zoom-extended-1.21.5-forge-1.1.0.jar` | `HzvpAPjA` |
| `cinematic-zoom-extended-1.21.7-1.21.8-fabric-1.1.0.jar` | `rzon62yb` |
| `cinematic-zoom-extended-1.21.7-neoforge-1.1.0.jar` | `oQtEaJGH` |
| `cinematic-zoom-extended-1.21.7-forge-1.1.0.jar` | `6BicYa9y` |
| `cinematic-zoom-extended-1.21.9-1.21.11-fabric-1.1.0.jar` | `Lckvup60` |
| `cinematic-zoom-extended-1.21.11-neoforge-1.1.0.jar` | `mXQsgtJW` |
| `cinematic-zoom-extended-1.21.11-forge-1.1.0.jar` | `H2lEuYEx` |
| `cinematic-zoom-extended-26.1-fabric-1.1.0.jar` | `9t8SV3Ei` |
| `cinematic-zoom-extended-26.1-neoforge-1.1.0.jar` | `SIHvkve2` |
| `cinematic-zoom-extended-26.1-forge-1.1.0.jar` | `WTcEfnlX` |
| `cinematic-zoom-extended-26.2-fabric-1.1.0.jar` | `7u9RWH54` |
| `cinematic-zoom-extended-26.2-neoforge-1.1.0.jar` | `Jyh0Jx1n` |
| `cinematic-zoom-extended-26.2-forge-1.1.0.jar` | `Ud1gGvIM` |
| `cinematic-zoom-extended-26.3-fabric-1.1.0.jar` | `LawMpUIQ` |
| `cinematic-zoom-extended-26.3-neoforge-1.1.0.jar` | `cw1YK1qo` |
| `cinematic-zoom-extended-26.3-forge-1.1.0.jar` | `ZgvOP0tJ` |

The uploaded 1.0.0 files use channel `release` and loaders `fabric` and `quilt`. The table records their original filenames. New builds use `cinematic-zoom-extended-<jar>-<version>.jar`; existing uploads retain their filenames and version identities. Quilt loads the Fabric jars.

Keep the current uploads rather than uploading replacements for this repository rename.

| JAR | Version ID | Modrinth `game_versions` |
| --- | --- | --- |
| `cinematiczoom-extended-1.20.1-1.0.0.jar` | `BJswwyUg` | `1.20.1` |
| `cinematiczoom-extended-1.21.1-1.0.0.jar` | `IB88SkKT` | `1.21.1` |
| `cinematiczoom-extended-1.21.2-1.21.4-1.0.0.jar` | `UG64B2Oi` | `1.21.2`, `1.21.3`, `1.21.4` |
| `cinematiczoom-extended-1.21.5-1.21.6-1.0.0.jar` | `mg1s3KGm` | `1.21.5`, `1.21.6` |
| `cinematiczoom-extended-1.21.7-1.21.8-1.0.0.jar` | `Cyb8tdv9` | `1.21.7`, `1.21.8` |
| `cinematiczoom-extended-1.21.9-1.21.11-1.0.0.jar` | `XeUX9PSG` | `1.21.9`, `1.21.10`, `1.21.11` |
| `cinematiczoom-extended-26.1-1.0.0.jar` | `WdStVaNU` | `26.1`, `26.1.1`, `26.1.2` |
| `cinematiczoom-extended-26.2-1.0.0.jar` | `O4IzsBfh` | `26.2` (Modrinth has no `26.2.1`/`26.2.2` tags yet) |
| `cinematiczoom-extended-26.3-1.0.0.jar` | `jJlQiC12` | `26.3` |

Version name format: `Cinematic Zoom Extended 1.0.0 (1.20.1)` or `Cinematic Zoom Extended 1.0.0 (1.21.2–1.21.4)`.

### Dependencies

| Project | ID | Type | Applies to |
| --- | --- | --- | --- |
| Fabric API | `P7dR8mSH` | Required | Fabric |
| Cloth Config API | `9s6osm5g` | Required | Fabric, NeoForge, Forge 1.20.1 |
| Mod Menu | `mOgUt4GM` | Required | Fabric |
| Cinematic Zoom (Fabric) | `LBKcRJTu` | Incompatible | Fabric |

## Gallery

Upload in this order, with `cinematic` featured. Gallery files must be under **5 MiB** each.

| File | Title | Description |
| --- | --- | --- |
| `art/gallery/cinematic.png` | Cinematic zoom | Hold C and the view eases in while black bars slide across the screen. The HUD, crosshair and hand can each be hidden. |
| `art/gallery/aspect-ratios.png` | Aspect ratios | Press the left and right arrow keys while zoomed to move between profiles like 2.39:1, 4:3 and 1:1. |
| `art/gallery/regular-zoom.png` | Regular zoom | Hold V for a second profile with no bars and its own magnification, shown here at 10×. |
| `art/gallery/settings.png` | Settings | Each profile keeps its own zoom, bar size, duration and easing curve. Open them from Mod Menu. |

---

Smooth, configurable zoom with animated cinema bars.

Hold a key and the view eases in while black bars slide across the screen, then eases back out when you let go. Cinematic Zoom Extended adds two independent profiles, six easing curves, ultra-wide zoom, snap targets and fine control over what the HUD hides.

A fork of [Cinematic Zoom (Fabric)](https://modrinth.com/mod/cinematic-zoom-fabric) by [Mel1x](https://github.com/mel1x), based on the original [Cinematic Zoom](https://modrinth.com/mod/cinematiczoom-original) by [KubePixel](https://modrinth.com/user/KubePixel). Published with permission. Not affiliated with or endorsed by either author.

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
