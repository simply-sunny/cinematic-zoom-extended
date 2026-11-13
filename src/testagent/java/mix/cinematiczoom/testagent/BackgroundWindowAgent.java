package mix.cinematiczoom.testagent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.function.UnaryOperator;

/**
 * Keeps test game windows behind whatever you're working in. Every loader and version creates its
 * windows through LWJGL's GLFW class, so this patches that one class: windows are created without
 * focus, showing one doesn't focus it, and glfwFocusWindow does nothing. An unfocused window also
 * can't capture the mouse. Only test runs load this agent; release jars never contain it.
 */
public final class BackgroundWindowAgent {
    private static final String GLFW = "org/lwjgl/glfw/GLFW";

    private BackgroundWindowAgent() {
    }

    @SuppressWarnings("unchecked")
    public static void premain(String args, Instrumentation instrumentation) throws Exception {
        // GlfwPatcher and its ASM copy sit under patcher/ in this jar, in their own class loader:
        // Fabric refuses to start if a second ASM shows up on the game's classpath.
        URL jar = BackgroundWindowAgent.class.getProtectionDomain().getCodeSource().getLocation();
        ClassLoader isolated = new URLClassLoader(new URL[] {new URL("jar:" + jar + "!/patcher/")}, ClassLoader.getPlatformClassLoader());
        UnaryOperator<byte[]> patcher = (UnaryOperator<byte[]>) isolated.loadClass("mix.cinematiczoom.testagent.GlfwPatcher").getConstructor().newInstance();
        instrumentation.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String name, Class<?> redefined, ProtectionDomain domain, byte[] bytes) {
                if (!GLFW.equals(name)) return null;
                try {
                    byte[] patched = patcher.apply(bytes);
                    System.err.println("[background-window] GLFW windows will open unfocused");
                    return patched;
                } catch (Throwable error) {
                    System.err.println("[background-window] could not patch GLFW: " + error);
                    return null;
                }
            }
        });
    }
}
