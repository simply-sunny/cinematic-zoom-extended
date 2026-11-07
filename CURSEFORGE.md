# CurseForge release sheet

This file records the CurseForge project settings and uploaded release filenames. The listing body starts after the horizontal rule. It matches `MODRINTH.md` except that mod links stay on CurseForge. Gallery images, titles and descriptions come from the Gallery table in `MODRINTH.md`.

## Project settings

| Field | Value |
| --- | --- |
| Name | Cinematic Zoom Extended |
| Slug | `cinematic-zoom-extended` |
| Project ID | `1763623` |
| Class | Mods |
| Summary | A configurable zoom with animated cinema bars and two profiles. |
| License | MIT |
| Environment | Client-side only |
| Loaders | Fabric, Quilt, Forge, NeoForge |
| Avatar | `src/main/resources/assets/cinematiczoom/icon.png` |
| Source | https://github.com/simply-sunny/cinematic-zoom-extended |
| Issues | https://github.com/simply-sunny/cinematic-zoom-extended/issues |
| Wiki / docs | https://simply-sunny.github.io/cinematic-zoom-extended/ |
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

The uploaded 1.2.0 files and their file IDs:

| JAR | File ID |
| --- | --- |
| `cinematic-zoom-extended-1.20.1-fabric-1.2.0.jar` | `9486589` |
| `cinematic-zoom-extended-1.20.1-neoforge-1.2.0.jar` | `9486590` |
| `cinematic-zoom-extended-1.20.1-forge-1.2.0.jar` | `9486591` |
| `cinematic-zoom-extended-1.21.1-fabric-1.2.0.jar` | `9486592` |
| `cinematic-zoom-extended-1.21.1-neoforge-1.2.0.jar` | `9486593` |
| `cinematic-zoom-extended-1.21.1-forge-1.2.0.jar` | `9486594` |
| `cinematic-zoom-extended-1.21.2-1.21.4-fabric-1.2.0.jar` | `9486595` |
| `cinematic-zoom-extended-1.21.2-neoforge-1.2.0.jar` | `9486596` |
| `cinematic-zoom-extended-1.21.5-1.21.6-fabric-1.2.0.jar` | `9486597` |
| `cinematic-zoom-extended-1.21.5-neoforge-1.2.0.jar` | `9486598` |
| `cinematic-zoom-extended-1.21.5-forge-1.2.0.jar` | `9486599` |
| `cinematic-zoom-extended-1.21.7-1.21.8-fabric-1.2.0.jar` | `9486600` |
| `cinematic-zoom-extended-1.21.7-neoforge-1.2.0.jar` | `9486601` |
| `cinematic-zoom-extended-1.21.7-forge-1.2.0.jar` | `9486602` |
| `cinematic-zoom-extended-1.21.9-1.21.11-fabric-1.2.0.jar` | `9486603` |
| `cinematic-zoom-extended-1.21.11-neoforge-1.2.0.jar` | `9486604` |
| `cinematic-zoom-extended-1.21.11-forge-1.2.0.jar` | `9486605` |
| `cinematic-zoom-extended-26.1-fabric-1.2.0.jar` | `9486606` |
| `cinematic-zoom-extended-26.1-neoforge-1.2.0.jar` | `9486607` |
| `cinematic-zoom-extended-26.1-forge-1.2.0.jar` | `9486608` |
| `cinematic-zoom-extended-26.2-fabric-1.2.0.jar` | `9486609` |
| `cinematic-zoom-extended-26.2-neoforge-1.2.0.jar` | `9486610` |
| `cinematic-zoom-extended-26.2-forge-1.2.0.jar` | `9486611` |
| `cinematic-zoom-extended-26.3-fabric-1.2.0.jar` | `9486612` |
| `cinematic-zoom-extended-26.3-neoforge-1.2.0.jar` | `9486613` |
| `cinematic-zoom-extended-26.3-forge-1.2.0.jar` | `9486614` |

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

A fork of [Cinematic Zoom (Fabric)](https://www.curseforge.com/minecraft/mc-mods/cinematic-zoom-fabric) by [Mel1x](https://github.com/mel1x), based on [Cinematic Zoom](https://www.curseforge.com/minecraft/mc-mods/cinematic-zoom) by [KubePixel](https://www.curseforge.com/members/kubepixel/projects). Published with permission. Not affiliated with or endorsed by Mojang, Microsoft, Mel1x or KubePixel.

MIT, see the [LICENSE](https://github.com/simply-sunny/cinematic-zoom-extended/blob/main/LICENSE). The icon's curtain texture is by ColoredChromium ([CC BY 3.0](https://creativecommons.org/licenses/by/3.0/), [source](https://www.deviantart.com/coloredchromium/art/Curtains-Red-Transparency-Light-424984584)). Minecraft is a trademark of Mojang Studios.
