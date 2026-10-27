# cinematic-zoom-extended

## Build and verify

- Use JDK 25. Run `./gradlew test buildAll` for the regression suite and all nine release jars in `build/libs/`.
- For one target, run `./gradlew -PonlyTarget=26.3 :26.3:build`. Read `build.gradle` and `settings.gradle` before changing the supported targets or shared source sets.
- After a command fails, inspect its error and relevant files before retrying.

## Compatibility

- Preserve the mod ID `cinematiczoom_extended`, package `mix.cinematiczoom`, asset namespace `cinematiczoom`, and config path `config/cinematiczoom.json`. These names keep existing installations, key binds, and settings compatible.
- Use `cinematic-zoom-extended` for the repository and new artifact filenames. Published 1.0.0 assets and the release tag retain their original names and contents.

## Documentation and releases

- Before editing documentation, read the shared [writing rules](https://github.com/simply-sunny/templates/blob/main/docs/WRITING.md) and [document inventory](https://github.com/simply-sunny/templates/blob/main/docs/WHAT_TO_WRITE.md). Follow the [mod README skeleton](https://github.com/simply-sunny/templates/blob/main/docs/mod-README-skel.md).
- Keep user-facing changes under `[Unreleased]` in `CHANGELOG.md`. Use the project settings and listing copy in `MODRINTH.md` and `CURSEFORGE.md` for publishing.
- The existing Modrinth project is under verification. Preserve its project ID and uploads when maintaining repository links.
- Write concise, factual commit subjects. Never add AI attribution or co-authorship lines to commits, PRs, or changelogs.
