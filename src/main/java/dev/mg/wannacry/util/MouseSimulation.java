package dev.mg.wannacry.util;

import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public final class MouseSimulation {

    private MouseSimulation() {}

    public static void mouseClick(int button) {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().handle();
        mc.execute(() -> invokeOnPress(mc, window, button, GLFW.GLFW_PRESS));
        mc.execute(() -> invokeOnPress(mc, window, button, GLFW.GLFW_RELEASE));
    }

    public static void mouseClick(int button, int holdMs) {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().handle();
        mc.execute(() -> invokeOnPress(mc, window, button, GLFW.GLFW_PRESS));
        Thread t = new Thread(() -> {
            try { Thread.sleep(holdMs); } catch (InterruptedException ignored) {}
            mc.execute(() -> invokeOnPress(mc, window, button, GLFW.GLFW_RELEASE));
        }, "satellite-mouse-sim");
        t.setDaemon(true);
        t.start();
    }

    public static void mouseRelease(int button) {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().handle();
        mc.execute(() -> invokeOnPress(mc, window, button, GLFW.GLFW_RELEASE));
    }

    private enum Variant { UNKNOWN, LEGACY_INT, MODERN_BUTTON_INFO, FAILED }

    private static volatile Variant   variant    = Variant.UNKNOWN;
    private static volatile Method    method     = null;
    private static volatile Constructor<?> mouseButtonInfoCtor = null;

    private static void invokeOnPress(Minecraft mc, long window, int button, int action) {
        try {
            if (variant == Variant.UNKNOWN) resolve(mc);

            switch (variant) {
                case LEGACY_INT -> method.invoke(mc.mouseHandler, window, button, action, 0);
                case MODERN_BUTTON_INFO -> {
                    Object info = mouseButtonInfoCtor.newInstance(button);
                    method.invoke(mc.mouseHandler, window, info, action);
                }
                default -> {}
            }
        } catch (Exception ignored) {}
    }

    private static synchronized void resolve(Minecraft mc) {
        if (variant != Variant.UNKNOWN) return;

        Class<?> handlerClass = mc.mouseHandler.getClass();

        for (String name : new String[]{"onButton", "m_91470_"}) {
            for (Class<?> mc_class : handlerClass.getDeclaredClasses()) {

            }

            for (Method m : handlerClass.getDeclaredMethods()) {
                Class<?>[] params = m.getParameterTypes();
                if (!m.getName().equals(name) && !m.getName().equals("m_91470_")) continue;
                if (params.length == 3
                        && params[0] == long.class
                        && params[2] == int.class
                        && params[1] != int.class) {

                    try {
                        Constructor<?> ctor = params[1].getDeclaredConstructor(int.class);
                        ctor.setAccessible(true);
                        m.setAccessible(true);
                        method = m;
                        mouseButtonInfoCtor = ctor;
                        variant = Variant.MODERN_BUTTON_INFO;
                        return;
                    } catch (Exception ignored) {}
                }
                if (params.length == 4
                        && params[0] == long.class
                        && params[1] == int.class) {

                    m.setAccessible(true);
                    method = m;
                    variant = Variant.LEGACY_INT;
                    return;
                }
            }
        }

        for (String name : new String[]{"onPress", "m_91530_"}) {
            try {
                Method m = handlerClass.getDeclaredMethod(name, long.class, int.class, int.class, int.class);
                m.setAccessible(true);
                method = m;
                variant = Variant.LEGACY_INT;
                return;
            } catch (NoSuchMethodException ignored) {}
        }

        variant = Variant.FAILED;
    }
}
