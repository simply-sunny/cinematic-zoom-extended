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
| Summary | A configurable zoom with animated cinema bars and two profiles. |
| License | MIT |
| Environment | Client-side only (`client_only`) |
| Featured categories | `utility` |
| Additional categories | *(none)* |
| Source | https://github.com/simply-sunny/cinematic-zoom-extended |
| Issues | https://github.com/simply-sunny/cinematic-zoom-extended/issues |
| Wiki / docs | https://simply-sunny.github.io/cinematic-zoom-extended/ |
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

A configurable zoom with animated cinema bars.

Hold a key and the view eases in while bars slide across the screen. Let go and it eases back.

- Two profiles, each with its own key and settings
- Six easing curves
- Zoom from 0.5× to 500×
- Aspect ratios from 2.39:1 to 1:1, or your own
- Snap targets like `2x` or `45fov`
- Hide the HUD, crosshair and hands separately
- Imports Zoomify, Ok Zoomer and Just Zoom settings

## Installation

For Minecraft 1.20.1 to 26.3 on Fabric, Quilt, Forge or NeoForge. Client-side only.

1. Install these for your Minecraft version:
   - Fabric or Quilt: Fabric API, Cloth Config and Mod Menu
   - NeoForge and Forge 1.20.1: Cloth Config
2. Put the jar for your version and loader in `mods`.
3. Remove any other Cinematic Zoom build.

## More

Settings and every supported version are on the [wiki](https://simply-sunny.github.io/cinematic-zoom-extended/).

A fork of [Cinematic Zoom (Fabric)](https://modrinth.com/mod/cinematic-zoom-fabric) by [Mel1x](https://github.com/mel1x), based on [Cinematic Zoom](https://modrinth.com/mod/cinematiczoom-original) by [KubePixel](https://modrinth.com/user/KubePixel). Published with permission. Not affiliated with or endorsed by either author.

MIT, see the [LICENSE](https://github.com/simply-sunny/cinematic-zoom-extended/blob/main/LICENSE). The icon's curtain texture is by ColoredChromium ([CC BY 3.0](https://creativecommons.org/licenses/by/3.0/), [source](https://www.deviantart.com/coloredchromium/art/Curtains-Red-Transparency-Light-424984584)). Minecraft is a trademark of Mojang Studios.
