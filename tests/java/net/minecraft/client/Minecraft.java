package net.minecraft.client;

public final class Minecraft {
    private static Minecraft instance;
    public Minecraft() { instance = this; }
    public static Minecraft getInstance() { return instance; }
    public Window getWindow() { return new Window(); }
    public static final class Window { public int getWidth() { return 1920; } public int getHeight() { return 1080; } }
    public Object level = new Object();
    public net.minecraft.client.player.LocalPlayer player = new net.minecraft.client.player.LocalPlayer();
    public final Options options = new Options();
    public final Gui gui = new Gui();
    public Object screen;
    public final Object font = new Object();
    public boolean focused = true;

    public boolean isWindowActive() {
        return focused;
    }

    public static final class Options {
        public boolean smoothCamera;
        public CameraType cameraType = CameraType.FIRST_PERSON;

        public CameraType getCameraType() {
            return cameraType;
        }
    }

    public enum CameraType {
        FIRST_PERSON, THIRD_PERSON_BACK;

        public boolean isFirstPerson() {
            return this == FIRST_PERSON;
        }
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
