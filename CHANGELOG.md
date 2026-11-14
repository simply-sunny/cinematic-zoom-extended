# Changelog

All notable changes to Cinematic Zoom Extended are documented here. This project follows [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.3.0] - 2026-11-13

### Added

- Spyglass mode: a setting in General that makes a spyglass zoom with the cinematic or regular profile instead of showing its scope. The zoom lasts as long as you hold the spyglass up, and the zoom keys do nothing while it's on. Off by default. (mel1x/CinematicZoom#9)
- Scroll ticks: a soft tick for each mouse wheel step while zooming, higher going in and lower going out. On by default. (#2)
- Zoom sounds: the spyglass sounds play when any zoom starts and ends, not only a spyglass zoom. They don't show in subtitles. On by default.
- Sound settings: wheel ticks, zoom sounds and the screenshot sound have their own Sound page, and the screenshot sound can be turned off apart from the shutter.
- Zoom stacking: a setting in General. On, looking through a spyglass during a zoom multiplies the two. Off, the spyglass takes over while it's up and the zoom comes back when you lower it. On by default.
- Keep vignette: a per-profile setting that keeps the darkened screen corners during a zoom, fitted inside the bars and following them as they move, instead of hiding them with the HUD. On for cinematic, off for regular. Fabric 26.2 and later.
- Zoom percentage: the magnification, such as 300%, shows above the hotbar when a zoom starts or you scroll, then fades. On by default. (#1)
- Screenshot shutter: black bars snap shut and open when you take a screenshot, with a click as they shut and a softer one as they open. The saved screenshot doesn't include them. Works alongside screenshot mods such as Screencapper on 26.2 and later. On by default. (#3)
- Full frame rate HUD while zooming: mods that draw the HUD at a lower frame rate, such as Gnetum, are switched off while a zoom is on screen, so the bars move as smoothly as the game. On by default.

### Changed

- Bar animations default to Smooth instead of Linear for entry, exit and profile changes. Settings saved as Linear by 1.2 are switched to Smooth, since that was the old default. Other choices are kept, and Linear can be picked again.
- The Auto aspect ratio is gone. It showed no bars, which the name didn't say. Saved lists drop it, and a zoom that used it switches to the first ratio left, usually 2.39:1.

### Fixed

- Flashback: zoom works in replays. Flashback's replay camera set its own FOV and skipped the zoom. Scrolling to zoom in the replay editor no longer also changes the camera's fly speed. (mel1x/CinematicZoom#10)
- Shared keys: during a cinematic zoom, the bar and aspect keys go to the zoom only, so another mod on the same keys, such as Gamma Utils on the arrows, doesn't also react. Outside a cinematic zoom those mods get the keys as usual. Fabric 26.1 to 26.3.
- Hidden HUD: when a zoom hides the HUD, it now hides all of it the way F1 does, including chat, the scoreboard and HUDs from mods that follow F1, such as VanillaHUD and EvergreenHUD. The zoom's own hand setting still controls the hand. Flashback's replay editor stays open, so the zoom stays inside its game view. 26.2 and later.

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
