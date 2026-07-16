package dev.mg.wannacry.util;

public final class PacketGuard {
    private PacketGuard() {}

    private static final ThreadLocal<Boolean> SENDING = ThreadLocal.withInitial(() -> false);

    public static void begin()           { SENDING.set(true);  }
    public static void end()             { SENDING.set(false); }
    public static boolean isSending()    { return SENDING.get(); }
}
