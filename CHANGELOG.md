# Changelog

All notable changes to Cinematic Zoom Extended are documented here. This project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Changed

- Repository: use `cinematic-zoom-extended` for GitHub, the Gradle project, and new build filenames. Existing release downloads, mod identity, key binds, and saved settings keep their names and contents.
- CurseForge publishing: read the version and artifact prefix from `gradle.properties`.

### Documentation

- Update source, issue, release, and badge links to the renamed repository.
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
