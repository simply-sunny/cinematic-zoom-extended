# CurseForge release sheet

This file records the CurseForge project settings and uploaded release filenames. `MODRINTH.md` contains the shared listing body after its horizontal rule.

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
| Description | The listing body from `MODRINTH.md`, everything after the horizontal rule. |
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
