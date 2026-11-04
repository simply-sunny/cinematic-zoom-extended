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
| Loaders | Fabric, Quilt |
| Avatar | `src/main/resources/assets/cinematiczoom/icon.png` |
| Description | The listing body from `MODRINTH.md`, everything after the horizontal rule. |
| Source | https://github.com/simply-sunny/cinematic-zoom-extended |
| Issues | https://github.com/simply-sunny/cinematic-zoom-extended/issues |
| Wiki / docs | https://github.com/simply-sunny/cinematic-zoom-extended#configure |
| Required dependencies | Fabric API (`fabric-api`), Cloth Config (`cloth-config`), Mod Menu (`modmenu`) |

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

New builds use `cinematic-zoom-extended-<jar>-<version>.jar`. The upload script reads the version and prefix from `gradle.properties`. Keep the existing 1.0.0 uploads when applying the repository rename; bump `mod_version` and add release notes before publishing changed jars.

```bash
./gradlew test buildAll
python3 scripts/publish-curseforge.py 1763623 --dry-run
```

Set `CURSEFORGE_API_KEY` in the environment for the dry run or upload. The dry run reads CurseForge version tags and prints upload metadata. Remove `--dry-run` only when publishing a new release.
