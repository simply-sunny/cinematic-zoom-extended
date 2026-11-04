# Release verification — in progress

Do not publish while this report contains pending primary checks.

- Shared suite: 40 existing regression checks and 328 feature assertions pass.
- NeoForge: all nine primary targets pass the expanded real-client suite (seven feature scenarios, nine captures each), including native legacy 1.20.1.
- Forge: real-client matrix running; 1.21.2 is unavailable upstream.
- Fabric: expanded suite pending final run on all nine targets. Baseline clients and feature builds passed earlier.
- Quilt: actual-loader compatibility run pending. No compatibility claim is inferred from Fabric success.
- Packaging, pixel verification, final diff review, and remote checks remain release gates.

The engine tests cover 1920×1080, 1280×720, 1600×1200, 1024×1024, 3440×1440, and 800×600 geometry. Current desktop captures use a 1280×720 logical window; macOS may produce a larger framebuffer. Compare expected bars using framebuffer size and GUI-scale rounding tolerance.

Compatibility with shaders, Sodium/Iris, OptiFine, and additional patch versions is not established by these tests.
