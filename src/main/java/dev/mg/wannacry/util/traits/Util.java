package dev.mg.wannacry.util.traits;

import dev.mg.wannacry.event.system.EventBus;
import net.minecraft.client.Minecraft;

public interface Util {
    Minecraft mc = Minecraft.getInstance();
    EventBus EVENT_BUS = new EventBus();

    static boolean isLocalPlayer(Object o) {
        return o == mc.player;
    }
}
