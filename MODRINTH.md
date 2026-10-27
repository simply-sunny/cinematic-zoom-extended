# Modrinth release sheet

This file is the source of truth for the Modrinth project settings, version metadata, and listing body. The listing body starts after the horizontal rule.

## Project settings

| Field | Value |
| --- | --- |
| Title | **Cinematic Zoom Extended** |
| Slug | `cinematic-zoom-extended` |
| Project type | Mod |
| Project ID | `nGcifVtU` |
| Status | In review on Modrinth as of 2026-10-27 (`processing`); retain the existing project and uploaded versions. |
| CurseForge project ID | `1763623` (slug `cinematic-zoom-extended`), uploaded with `scripts/publish-curseforge.py` |
| Summary | Configurable zoom with two profiles, animated cinema bars, easing curves, ultra-wide zoom, snap targets, and HUD controls. Fabric and Quilt 1.20.1–26.3, client-side. |
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

| Project | ID | Type |
| --- | --- | --- |
| Fabric API | `P7dR8mSH` | Required |
| Cloth Config API | `9s6osm5g` | Required |
| Mod Menu | `mOgUt4GM` | Required |
| Cinematic Zoom (Fabric) | `LBKcRJTu` | Incompatible |

## Gallery plan

Gallery files must be under **5 MiB** each. Use PNG or JPEG screenshots.

| Order | Asset | Caption |
| --- | --- | --- |
| 1 | Hero screenshot, 1280×720 | Cinematic zoom with animated black bars |
| 2 | Settings screen | Two independent profiles: Cinematic and Regular |
| 3 | Ultra-wide/snap screenshot | Ultra-wide zoom and snap targets |
| 4 | HUD comparison | Hide the HUD, crosshair, and hands independently |

Alt text should repeat the caption.

---

Smooth, configurable zoom with animated cinema bars.

Hold a key and the view eases in while black bars slide across the screen, then eases back out when you let go. Cinematic Zoom Extended adds two independent profiles, six easing curves, ultra-wide zoom, snap targets and fine control over what the HUD hides.

A fork of [Cinematic Zoom (Fabric)](https://modrinth.com/mod/cinematic-zoom-fabric) by [Mel1x](https://github.com/mel1x), based on the original [Cinematic Zoom](https://www.curseforge.com/minecraft/mc-mods/cinematic-zoom) by [KubePixel](https://www.curseforge.com/members/kubepixel/projects). Published with permission. Not affiliated with or endorsed by either author.

## Features

- Two profiles. Cinematic (with bars) and Regular (without), each with its own key, zoom, timing, curves, toggle mode, snap targets and HUD options.
- Smooth motion. Per-frame easing with six curves and separate in and out durations.
- Framing. Adjust the bars live with the arrow keys, zoom out to 0.5x with ultra-wide, step the wheel in fixed increments, and snap to targets like `2x` or `45fov`.
- Camera and HUD. Scale mouse sensitivity, remove view bobbing, and hide the HUD, crosshair and hands independently. F3 hides with the HUD.
- Settings screen. Cloth Config through Mod Menu.
- Easy switching. Cinematic Zoom 1.3 configs migrate automatically, and Zoomify, Ok Zoomer, Just Zoom and similar mods are imported on first launch.

## Installation

1. Install Fabric Loader, Fabric API, Cloth Config and Mod Menu for your Minecraft version.
2. Put the jar for your Minecraft version in `mods`.
3. Remove any other Cinematic Zoom build; only one can be installed.

## Usage

| Action | Default key |
| --- | --- |
| Cinematic zoom | `C` |
| Zoom without bars | Unbound, set in Controls |
| Increase bar size | `↑` |
| Decrease bar size | `↓` |

Hold the zoom key to zoom and release to return, or turn on **Toggle zoom**. Scroll the mouse wheel while zooming to change magnification. Settings are under **Mod Menu → Cinematic Zoom Extended**.

## Compatibility

- **Minecraft:** 1.20.1 to 26.3 on Fabric and Quilt. Java 17 for 1.20.1, 21 for 1.21.x, 25 for 26.x.
- Client-side only. Servers do not need it.
- Other zoom mods conflict with FOV handling. Import their settings instead of running both.
- **Sodium, Iris and shaders:** no known conflicts, not specifically tested in 1.0.0.
- Quilt is supported (it runs the Fabric jar with Fabric API). OptiFine is unsupported.

Full configuration, import and troubleshooting notes are on [GitHub](https://github.com/simply-sunny/cinematic-zoom-extended#readme).

## Credits and license

Thanks to KubePixel for the original concept, Mel1x for the Fabric implementation, and ColoredChromium for the velvet curtain texture in the icon ([CC BY 3.0](https://creativecommons.org/licenses/by/3.0/), [source](https://www.deviantart.com/coloredchromium/art/Curtains-Red-Transparency-Light-424984584)). Mod code and original assets are MIT, see the [LICENSE](https://github.com/simply-sunny/cinematic-zoom-extended/blob/main/LICENSE). The icon texture remains CC BY 3.0. Minecraft is a trademark of Mojang Studios.
