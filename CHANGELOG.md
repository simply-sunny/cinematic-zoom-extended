# Changelog

All notable changes to Cinematic Zoom Extended are documented here. This project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.2.0] - 2026-11-06

### Changed

- Everything the mod saves is now in `config/cinematiczoom/`: `config.json` and `backups/`. Files from older versions move there on launch.

## [1.1.0] - 2026-11-04

### Added

- Forge and NeoForge: native builds for 1.20.1, 1.21.1, 1.21.5, 1.21.7, 1.21.11, 26.1, 26.2, and 26.3, plus NeoForge 1.21.2. Forge published no 1.21.2 loader. Each jar loads only on its exact Minecraft version.
- Aspect ratio profiles: Auto, 2.39:1, 2.35:1, 2:1, 1.85:1, 16:9, 16:10, 4:3, and 1:1, plus custom ratios. Ratios wider than the window add top and bottom bars. Narrower ratios add side bars.
- Profile cycling: `←` and `→` switch ratios during cinematic zoom. Enable global cycling to switch outside zoom. The selection is saved.
- Bar animation: entry, exit, and profile changes each have their own Instant, Linear, or Smooth mode and duration.
- Accurate magnification up to 500× for new installations.
- Forge settings: a built-in settings screen on 1.21.1 and newer, with no Cloth Config or Mod Menu needed.

### Changed

- Zoom without bars: bind it to `V` by default. Players who already set this key keep their choice.
- Jar names: Fabric jars now include the loader, such as `cinematic-zoom-extended-1.21.2-1.21.4-fabric-1.1.0.jar`.
- 26.2: this jar now covers 26.2 only. 26.2.1 and 26.2.2 have no Fabric builds to test against.
- Existing settings: saved configs keep their FOV behavior, percentage bars, and zoom limits. Migration writes `cinematiczoom.json.v1.bak` first.
- Repository: use `cinematic-zoom-extended` for GitHub, the Gradle project, and new build filenames. Existing release downloads, mod identity, key binds, and saved settings keep their names and contents.
- CurseForge publishing: read the version and artifact prefix from `gradle.properties`, and upload every loader.
- Update source, issue, release, and badge links to the renamed repository.
- Wiki: interactive and animated zoom views, run on the mod's zoom code over the game's title screen panorama. Add left and right aspect presets and letterbox and pillarbox visuals.
- Add the CurseForge release sheet, configuration defaults, and repository instructions using the shared templates standard.

## [1.0.0] - 2026-10-20

First release, based on Cinematic Zoom 1.3 by Mel1x.

### Added

- Cinematic (bars) and Regular (no bars) zoom profiles with separate keys, zoom, timing, curves, toggle mode, snap targets, and HUD settings.
- Per-frame smoothing with six easing curves.
- Live arrow-key bar adjustment, ultra-wide zoom, discrete wheel steps, and snap/magnetize targets (`2x`, `45fov`).
- Sensitivity scaling, view-bobbing removal, and independent HUD/crosshair/hands hiding.
- 512×512 Extended icon.
- First-run import from Zoomify, Ok Zoomer, Just Zoom, and other zoom-named configs.
- Config migration from Cinematic Zoom 1.3.
- 40 headless regression checks.

### Changed

- New mod ID `cinematiczoom_extended` and name **Cinematic Zoom Extended**; original `cinematiczoom` declared incompatible.
- Cloth Config and Mod Menu are now required.
- Fixed Fabric API and Cloth Config declarations on 26.x.
- Version now comes from `gradle.properties`; JARs use `cinematiczoom-extended-<minecraft>-<version>.jar`.
- Updated metadata, links, translations, and credits.

### Fixed

- Profile switching preserves the current zoom.
- HUD, crosshair, and hands hide independently.
- 26.x builds no longer rely on undeclared runtime dependencies.
- Unix `gradlew` wrapper is executable.
