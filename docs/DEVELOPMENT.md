# Multi-loader development

The shared engine in `src/common/java` owns magnification, FOV math, profile selection, configuration, and normalized bar geometry. Adapters handle lifecycle, input, visibility, final drawing, and settings entry points. The engine has no loader dependency.

Run Gradle with Java 25. Install JDK 8, 17, and 21 as well (ForgeGradle’s launcher helper requests Java 8; the game itself still uses 21 or 25) for native toolchains. Set `JAVA_INSTALLATIONS` to a comma-separated list of JDK homes when running the Python client runner if Gradle cannot discover them.

```sh
./gradlew :test buildAll
./gradlew -p platform/forge-1.20.1 build
./gradlew -p platform/forge-1.20.1 build -PlegacyNeo
./gradlew -p platform/forge build -Pmc=1.21.11
./gradlew -p platform/neoforge build -Pmc=26.3
```

Fabric artifacts collect in `build/libs`; native artifacts are in the selected platform's `build/<target>/libs` (legacy uses `build/forge/libs` or `build/neoforge/libs`). All archives normalize ordering and timestamps. Release builds must omit `-PtestClient` and `-PquiltTest`.

## Pinned native targets

| Minecraft | Forge | NeoForge | Cloth Config on NeoForge |
| --- | --- | --- | --- |
| 1.20.1 | 47.4.28 | 47.1.106 (original fork) | Forge 11.1.136 |
| 1.21.1 | 52.1.20 | 21.1.264 | 15.0.140 |
| 1.21.2 | unavailable | 21.2.1-beta | 16.0.143 |
| 1.21.5 | 55.1.18 | 21.5.101 | 18.0.145 |
| 1.21.7 | 57.0.0 | 21.7.25-beta | 19.0.147 |
| 1.21.11 | 61.2.4 | 21.11.48 | 21.11.153 |
| 26.1 | 62.0.9 | 26.1.0.19-beta | 26.1.154 |
| 26.2 | 65.1.5 | 26.2.0.93 | 26.2.155 |
| 26.3 | 66.1.0 | 26.3.0.95 | 26.3.159 |

Forge uses ForgeGradle 7.0.40 for 1.21.1 onward. The legacy Forge adapter and NeoForge use ModDevGradle 2.0.148. Fabric's pinned dependencies remain in the root build file. The 1.20.1 NeoForge fork retains Forge package names and metadata, but resolves the native NeoForge loader. Newer NeoForge has a separate adapter.

## Real client checks

The client suites require Python 3 with Pillow. Every capture must pass `scripts/verify_captures.py`; successful game exit alone is insufficient. Run `python3 scripts/verify_captures.py --sheet sheet.png <captures>` to check captures again and get a contact sheet to look at.

```sh
OPEN_RESULTS=0 scripts/test-clients.sh
python3 scripts/test-platforms.py --loader forge
python3 scripts/test-platforms.py --loader neoforge
QUILT=1 OPEN_RESULTS=0 scripts/test-clients.sh
OPEN_RESULTS=0 scripts/test-clients.sh compat
QUILT=1 OPEN_RESULTS=0 scripts/test-clients.sh compat
```

`compat` runs each intermediate version a Fabric jar covers, such as 1.21.3 on the 1.21.2–1.21.4 jar. It compiles the mod against that version and replaces the demo worlds, so run the main suite with `CLEAN=1` afterward.

`test-clients.sh` compiles every target once, then runs 3 clients at a time. Set `JOBS` to change that. Windows stay behind other apps: the test marks the game focused without asking the OS.

These create isolated demo worlds and start each run from default settings. They exercise cinematic input, edge-triggered ratio switching, regular 500× zoom, exit, settings opening, intermediate linear/smooth bars, spyglass zoom, and the screenshot shutter. Fabric runs also zoom over a stand-in for Flashback's replay camera, which replaces the game's FOV the way Flashback does. A successful process exit alone is insufficient: each client must write `ok`. Captures and logs live under `build/testclient`. Test instrumentation is generated only with `-PtestClient` and must never appear in release jars. The clients start with their music off, and a Java agent (`src/testagent`, built by `./gradlew backgroundWindowAgent`) patches GLFW so their windows open unfocused and never take focus or the mouse.

There's no CI. Before a release, check everything locally: `./gradlew test`, `./gradlew buildAll` for the Fabric jars, `./gradlew -p platform/forge build -Pmc=<version>` and the same for `platform/neoforge` for each Forge and NeoForge target (`platform/forge-1.20.1` for 1.20.1, adding `-PlegacyNeo` for NeoForge), and the desktop test clients above.
