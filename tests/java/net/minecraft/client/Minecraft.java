package net.minecraft.client;

public final class Minecraft {
    private static Minecraft instance;
    public Minecraft() { instance = this; }
    public static Minecraft getInstance() { return instance; }
    public Window getWindow() { return new Window(); }
    public static final class Window { public int getWidth() { return 1920; } public int getHeight() { return 1080; } }
    public Object level = new Object();
    public Object player = new Object();
    public final Options options = new Options();
    public final Gui gui = new Gui();
    public Object screen;
    public boolean focused = true;

    public boolean isWindowActive() {
        return focused;
    }

    public static final class Options {
        public boolean smoothCamera;
    }

    public static final class Gui {
        public final Hud hud = new Hud();
        public Object screen;

        public Object screen() {
            return screen;
        }
    }

    public static final class Hud {
        private boolean hidden;

        public boolean isHidden() {
            return hidden;
        }

        public void toggle() {
            hidden = !hidden;
        }
    }
}
