# Release verification: 1.1.0

Verified on macOS on 2026-11-04. Each client ran the expanded suite: seven feature scenarios and nine captures, and every capture passed `scripts/verify_captures.py`.

| Loader | Minecraft | Result |
| --- | --- | --- |
| Fabric | 1.20.1, 1.21.1, 1.21.2, 1.21.5, 1.21.7, 1.21.11, 26.1, 26.2, 26.3 | Pass |
| Fabric | 1.21.3, 1.21.4, 1.21.6, 1.21.8, 1.21.9, 1.21.10, 26.1.1, 26.1.2 | Pass |
| Quilt | 1.20.1, 1.21.1, 1.21.2, 1.21.5, 1.21.7, 1.21.11, 26.1, 26.2, 26.3 | Pass |
| Quilt | 1.21.3, 1.21.4, 1.21.6, 1.21.8, 1.21.9, 1.21.10, 26.1.1, 26.1.2 | Pass |
| NeoForge | 1.20.1, 1.21.1, 1.21.2, 1.21.5, 1.21.7, 1.21.11, 26.1, 26.2, 26.3 | Pass |
| Forge | 1.20.1, 1.21.1, 1.21.5, 1.21.7, 1.21.11, 26.1, 26.2, 26.3 | Pass |

The shared suite of 40 regression checks and 328 feature assertions also passes.

Intermediate versions compile the mod against that Minecraft version. The resulting mod classes are identical to the classes in the released jar for that range. The released jars themselves were not launched on these versions.

## Limits

- Client runs are automated and drive key bindings directly. They don't press physical keys. Fabric, Forge, and NeoForge runs confirm that the no-bars key saves as `V` by default.
- Forge 1.21.1 and 1.21.5 tests join from the title screen. On those versions, Forge starts its event bus after quick play opens the world, and the first item drop crashes. This comes from Forge's startup order, and quick play can still hit it.
- Two client runs closed early with no error in the log: Fabric 1.20.1 and Forge 1.21.1. Forge 1.21.11 and 26.2 each failed a key-driven check once. All four passed on re-run. The causes are unknown.
- Some captures on fresh test worlds show dark patches in the scene, including normal captures taken before zooming. The bars are correct in those captures.
- Forge 1.21.7 draws the bars from `Gui.render`. Hiding the HUD with F1 wasn't tested there.
- 26.2.1 and 26.2.2 have no Fabric builds and aren't covered.
- Geometry tests cover 1920×1080, 1280×720, 1600×1200, 1024×1024, 3440×1440, and 800×600. Client captures use a 1280×720 logical window.
- Shaders, Sodium, Iris, OptiFine, Windows, and Linux aren't covered by these tests.
