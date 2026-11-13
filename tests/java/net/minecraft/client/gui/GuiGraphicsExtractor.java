package net.minecraft.client.gui;

// Rendering is outside these headless state/config checks.
public final class GuiGraphicsExtractor {
    public int guiWidth() {
        return 100;
    }

    public int guiHeight() {
        return 100;
    }

    private final Pose pose = new Pose();

    public Pose pose() {
        return pose;
    }

    /** Records the one level of translate and scale the zoom applies. */
    public static final class Pose {
        public float x, y, scaleX = 1, scaleY = 1;
        public int depth;

        public void pushMatrix() {
            depth++;
        }

        public void translate(float x, float y) {
            this.x += x;
            this.y += y;
        }

        public void scale(float x, float y) {
            scaleX *= x;
            scaleY *= y;
        }

        public void popMatrix() {
            depth--;
            x = y = 0;
            scaleX = scaleY = 1;
        }
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
