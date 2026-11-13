package net.minecraft.client.gui;

// Rendering is outside these headless state/config checks.
public final class GuiGraphicsExtractor {
    public int guiWidth() {
        return 100;
    }

    public int guiHeight() {
        return 100;
    }

    public final java.util.List<int[]> fills = new java.util.ArrayList<>();
    public final java.util.List<String> texts = new java.util.ArrayList<>();

    public void fill(int left, int top, int right, int bottom, int color) {
        fills.add(new int[]{left, top, right, bottom, color});
    }

    public void centeredText(Object font, String text, int x, int y, int color) {
        texts.add(text);
    }
}
