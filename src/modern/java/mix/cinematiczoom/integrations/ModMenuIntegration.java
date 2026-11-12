package mix.cinematiczoom.integrations;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;
import mix.cinematiczoom.ZoomConfig;
import mix.cinematiczoom.ZoomCurve;
import mix.cinematiczoom.BarAnimationMode;
import mix.cinematiczoom.FovMode;
import mix.cinematiczoom.SpyglassZoom;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ZoomConfig cfg = ZoomConfig.INSTANCE;
            cfg.clamp();

            ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("cinematiczoom.config.title"))
                .setSavingRunnable(cfg::save);

            ConfigEntryBuilder eb = builder.entryBuilder();

            // 1. Cinematic Zoom Category
            ConfigCategory cinematicCat = builder.getOrCreateCategory(Component.translatable("cinematiczoom.config.category.cinematic"));

            cinematicCat.addEntry(
                eb.startFloatField(Component.translatable("cinematiczoom.option.cinematic_starting_zoom"), cfg.cinematicStartingZoom)
                  .setMin(1f / cfg.maxZoomMultiplier).setMax(1f / cfg.minZoomMultiplier)
                  .setTooltip(Component.translatable("cinematiczoom.option.cinematic_starting_zoom.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicStartingZoom = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startFloatField(Component.translatable("cinematiczoom.option.bars_percent"), cfg.cinematicBarsPercent)
                  .setMin(0f).setMax(50f)
                  .setTooltip(Component.translatable("cinematiczoom.option.bars_percent.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicBarsPercent = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.arrow_keys_adjust_bars"), cfg.arrowKeysAdjustBars)
                  .setTooltip(Component.translatable("cinematiczoom.option.arrow_keys_adjust_bars.tooltip"))
                  .setSaveConsumer(v -> cfg.arrowKeysAdjustBars = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startFloatField(Component.translatable("cinematiczoom.option.bar_adjust_step"), cfg.barAdjustStep)
                  .setMin(0.1f).setMax(10.0f)
                  .setTooltip(Component.translatable("cinematiczoom.option.bar_adjust_step.tooltip"))
                  .setSaveConsumer(v -> cfg.barAdjustStep = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.smooth_bar_change"), cfg.smoothBarChange)
                  .setTooltip(Component.translatable("cinematiczoom.option.smooth_bar_change.tooltip"))
                  .setSaveConsumer(v -> cfg.smoothBarChange = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.preserve_bar_adjustment"), cfg.preserveBarAdjustment)
                  .setTooltip(Component.translatable("cinematiczoom.option.preserve_bar_adjustment.tooltip"))
                  .setSaveConsumer(v -> cfg.preserveBarAdjustment = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startIntField(Component.translatable("cinematiczoom.option.zoom_in_ms"), cfg.cinematicZoomInMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_in_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomInMs = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startIntField(Component.translatable("cinematiczoom.option.zoom_out_ms"), cfg.cinematicZoomOutMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_out_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomOutMs = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startEnumSelector(Component.translatable("cinematiczoom.option.zoom_in_curve"), ZoomCurve.class, cfg.cinematicZoomInCurve)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_in_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomInCurve = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startEnumSelector(Component.translatable("cinematiczoom.option.zoom_out_curve"), ZoomCurve.class, cfg.cinematicZoomOutCurve)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_out_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicZoomOutCurve = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.toggle_mode"), cfg.cinematicToggle)
                  .setTooltip(Component.translatable("cinematiczoom.option.toggle_mode.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicToggle = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.hide_hud"), cfg.cinematicHideHud)
                  .setTooltip(Component.translatable("cinematiczoom.option.hide_hud.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicHideHud = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.hide_crosshair"), cfg.cinematicHideCrosshair)
                  .setTooltip(Component.translatable("cinematiczoom.option.hide_crosshair.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicHideCrosshair = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.hide_hands"), cfg.cinematicHideHands)
                  .setTooltip(Component.translatable("cinematiczoom.option.hide_hands.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicHideHands = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.cinematic_cam"), cfg.cinematicCamera)
                  .setTooltip(Component.translatable("cinematiczoom.option.cinematic_cam.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicCamera = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.scale_sensitivity"), cfg.cinematicScaleSensitivity)
                  .setTooltip(Component.translatable("cinematiczoom.option.scale_sensitivity.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicScaleSensitivity = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.remove_bobbing"), cfg.cinematicRemoveBobbing)
                  .setTooltip(Component.translatable("cinematiczoom.option.remove_bobbing.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicRemoveBobbing = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.magnetize"), cfg.cinematicMagnetize)
                  .setTooltip(Component.translatable("cinematiczoom.option.magnetize.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicMagnetize = v)
                  .build()
            );

            cinematicCat.addEntry(
                eb.startStrField(Component.translatable("cinematiczoom.option.snap_points"), cfg.cinematicSnapPoints)
                  .setTooltip(Component.translatable("cinematiczoom.option.snap_points.tooltip"))
                  .setSaveConsumer(v -> cfg.cinematicSnapPoints = v)
                  .build()
            );

            // 2. Regular Zoom Category
            ConfigCategory regularCat = builder.getOrCreateCategory(Component.translatable("cinematiczoom.config.category.regular"));

            regularCat.addEntry(
                eb.startFloatField(Component.translatable("cinematiczoom.option.regular_starting_zoom"), cfg.regularStartingZoom)
                  .setMin(1f / cfg.maxZoomMultiplier).setMax(1f / cfg.minZoomMultiplier)
                  .setTooltip(Component.translatable("cinematiczoom.option.regular_starting_zoom.tooltip"))
                  .setSaveConsumer(v -> cfg.regularStartingZoom = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startIntField(Component.translatable("cinematiczoom.option.zoom_in_ms"), cfg.regularZoomInMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_in_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomInMs = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startIntField(Component.translatable("cinematiczoom.option.zoom_out_ms"), cfg.regularZoomOutMs)
                  .setMin(0).setMax(5000)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_out_ms.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomOutMs = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startEnumSelector(Component.translatable("cinematiczoom.option.zoom_in_curve"), ZoomCurve.class, cfg.regularZoomInCurve)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_in_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomInCurve = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startEnumSelector(Component.translatable("cinematiczoom.option.zoom_out_curve"), ZoomCurve.class, cfg.regularZoomOutCurve)
                  .setTooltip(Component.translatable("cinematiczoom.option.zoom_out_curve.tooltip"))
                  .setSaveConsumer(v -> cfg.regularZoomOutCurve = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.toggle_mode"), cfg.regularToggle)
                  .setTooltip(Component.translatable("cinematiczoom.option.toggle_mode.tooltip"))
                  .setSaveConsumer(v -> cfg.regularToggle = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.hide_hud"), cfg.regularHideHud)
                  .setTooltip(Component.translatable("cinematiczoom.option.hide_hud.tooltip"))
                  .setSaveConsumer(v -> cfg.regularHideHud = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.hide_crosshair"), cfg.regularHideCrosshair)
                  .setTooltip(Component.translatable("cinematiczoom.option.hide_crosshair.tooltip"))
                  .setSaveConsumer(v -> cfg.regularHideCrosshair = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.hide_hands"), cfg.regularHideHands)
                  .setTooltip(Component.translatable("cinematiczoom.option.hide_hands.tooltip"))
                  .setSaveConsumer(v -> cfg.regularHideHands = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.cinematic_cam"), cfg.regularCinematicCamera)
                  .setTooltip(Component.translatable("cinematiczoom.option.cinematic_cam.tooltip"))
                  .setSaveConsumer(v -> cfg.regularCinematicCamera = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.scale_sensitivity"), cfg.regularScaleSensitivity)
                  .setTooltip(Component.translatable("cinematiczoom.option.scale_sensitivity.tooltip"))
                  .setSaveConsumer(v -> cfg.regularScaleSensitivity = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.remove_bobbing"), cfg.regularRemoveBobbing)
                  .setTooltip(Component.translatable("cinematiczoom.option.remove_bobbing.tooltip"))
                  .setSaveConsumer(v -> cfg.regularRemoveBobbing = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.magnetize"), cfg.regularMagnetize)
                  .setTooltip(Component.translatable("cinematiczoom.option.magnetize.tooltip"))
                  .setSaveConsumer(v -> cfg.regularMagnetize = v)
                  .build()
            );

            regularCat.addEntry(
                eb.startStrField(Component.translatable("cinematiczoom.option.snap_points"), cfg.regularSnapPoints)
                  .setTooltip(Component.translatable("cinematiczoom.option.snap_points.tooltip"))
                  .setSaveConsumer(v -> cfg.regularSnapPoints = v)
                  .build()
            );

            // 3. General & Wheel Category
            ConfigCategory generalCat = builder.getOrCreateCategory(Component.translatable("cinematiczoom.config.category.general"));

            generalCat.addEntry(
                eb.startEnumSelector(Component.translatable("cinematiczoom.option.spyglass_zoom"), SpyglassZoom.class, cfg.spyglassZoom)
                  .setEnumNameProvider(v -> Component.translatable("cinematiczoom.option.spyglass_zoom." + v.name().toLowerCase(java.util.Locale.ROOT)))
                  .setTooltip(Component.translatable("cinematiczoom.option.spyglass_zoom.tooltip"))
                  .setSaveConsumer(v -> cfg.spyglassZoom = v)
                  .build()
            );

            generalCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.mute_music"), cfg.muteMusicWhileZooming)
                  .setTooltip(Component.translatable("cinematiczoom.option.mute_music.tooltip"))
                  .setSaveConsumer(v -> cfg.muteMusicWhileZooming = v)
                  .build()
            );

            generalCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.wheel_ticks"), cfg.wheelTicks)
                  .setTooltip(Component.translatable("cinematiczoom.option.wheel_ticks.tooltip"))
                  .setSaveConsumer(v -> cfg.wheelTicks = v)
                  .build()
            );

            generalCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.mouse_wheel_enabled"), cfg.mouseWheelEnabled)
                  .setTooltip(Component.translatable("cinematiczoom.option.mouse_wheel_enabled.tooltip"))
                  .setSaveConsumer(v -> cfg.mouseWheelEnabled = v)
                  .build()
            );

            generalCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.discrete_scroll"), cfg.discreteScroll)
                  .setTooltip(Component.translatable("cinematiczoom.option.discrete_scroll.tooltip"))
                  .setSaveConsumer(v -> cfg.discreteScroll = v)
                  .build()
            );

            generalCat.addEntry(
                eb.startFloatField(Component.translatable("cinematiczoom.option.discrete_scroll_step"), cfg.discreteScrollStep)
                  .setMin(0.1f).setMax(5.0f)
                  .setTooltip(Component.translatable("cinematiczoom.option.discrete_scroll_step.tooltip"))
                  .setSaveConsumer(v -> cfg.discreteScrollStep = v)
                  .build()
            );

            generalCat.addEntry(
                eb.startBooleanToggle(Component.translatable("cinematiczoom.option.allow_ultrawide"), cfg.allowUltrawide)
                  .setTooltip(Component.translatable("cinematiczoom.option.allow_ultrawide.tooltip"))
                  .setSaveConsumer(v -> {
                      cfg.allowUltrawide = v;
                      cfg.maxZoomMultiplier = v ? 2.0f : 1.0f;
                  })
                  .build()
            );

            ConfigCategory aspect = builder.getOrCreateCategory(Component.literal("Aspect ratio and bars"));
            aspect.addEntry(eb.startStrField(Component.literal("Selected aspect ratio (Auto, 2.39:1, 4:3, Legacy...)"), cfg.aspectRatio).setSaveConsumer(v -> cfg.aspectRatio = v).build());
            aspect.addEntry(eb.startStrList(Component.literal("Enabled profiles (ordered, custom W:H allowed)"), cfg.aspectProfiles).setSaveConsumer(v -> cfg.aspectProfiles = v).build());
            aspect.addEntry(eb.startBooleanToggle(Component.literal("Allow cycling outside cinematic zoom"), cfg.globalProfileCycling).setSaveConsumer(v -> cfg.globalProfileCycling = v).build());
            aspect.addEntry(eb.startBooleanToggle(Component.literal("Animate profile changes"), cfg.animateProfileCycle).setSaveConsumer(v -> cfg.animateProfileCycle = v).build());
            aspect.addEntry(eb.startEnumSelector(Component.literal("Entry animation"), BarAnimationMode.class, cfg.barEnterMode).setSaveConsumer(v -> cfg.barEnterMode = v).build());
            aspect.addEntry(eb.startIntField(Component.literal("Entry duration (ms)"), cfg.barEnterMs).setMin(0).setMax(60000).setSaveConsumer(v -> cfg.barEnterMs = v).build());
            aspect.addEntry(eb.startEnumSelector(Component.literal("Exit animation"), BarAnimationMode.class, cfg.barExitMode).setSaveConsumer(v -> cfg.barExitMode = v).build());
            aspect.addEntry(eb.startIntField(Component.literal("Exit duration (ms)"), cfg.barExitMs).setMin(0).setMax(60000).setSaveConsumer(v -> cfg.barExitMs = v).build());
            aspect.addEntry(eb.startEnumSelector(Component.literal("Profile change animation"), BarAnimationMode.class, cfg.barSwitchMode).setSaveConsumer(v -> cfg.barSwitchMode = v).build());
            aspect.addEntry(eb.startIntField(Component.literal("Profile change duration (ms)"), cfg.barSwitchMs).setMin(0).setMax(60000).setSaveConsumer(v -> cfg.barSwitchMs = v).build());
            aspect.addEntry(eb.startFloatField(Component.literal("Bar opacity (0–1)"), cfg.barOpacity).setMin(0f).setMax(1f).setSaveConsumer(v -> cfg.barOpacity = v).build());
            generalCat.addEntry(eb.startEnumSelector(Component.literal("FOV calculation"), FovMode.class, cfg.fovMode).setSaveConsumer(v -> cfg.fovMode = v).build());
            generalCat.addEntry(eb.startFloatField(Component.literal("Maximum magnification"), 1f / cfg.minZoomMultiplier).setMin(1f).setMax(500f).setSaveConsumer(v -> cfg.minZoomMultiplier = 1f / v).build());
            generalCat.addEntry(eb.startBooleanToggle(Component.literal("Logarithmic wheel zoom"), cfg.logarithmicScroll).setSaveConsumer(v -> cfg.logarithmicScroll = v).build());
            generalCat.addEntry(eb.startFloatField(Component.literal("Wheel sensitivity"), cfg.wheelStep).setMin(.01f).setMax(.25f).setSaveConsumer(v -> cfg.wheelStep = v).build());

            return builder.build();
        };
    }
}
