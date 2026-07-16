package dev.mg.wannacry.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public final class WannaCrySounds {

    public static SoundEvent UI_CLICK;

    private WannaCrySounds() {}

    public static void register() {
        UI_CLICK = registerEvent("wannacry", "ui_click");
    }

    private static SoundEvent registerEvent(String namespace, String path) {
        Identifier id = Identifier.fromNamespaceAndPath(namespace, path);
        SoundEvent event = SoundEvent.createVariableRangeEvent(id);
        Registry.register(BuiltInRegistries.SOUND_EVENT, id, event);
        return event;
    }
}
