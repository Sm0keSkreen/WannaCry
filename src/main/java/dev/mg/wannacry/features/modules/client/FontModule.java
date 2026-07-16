package dev.mg.wannacry.features.modules.client;

import dev.mg.wannacry.font.FontType;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.settings.Setting;

public class FontModule extends Module {

    public final Setting<FontType>      fontType      = mode("Font",        FontType.ARIAL);

    public final Setting<Integer>       glyphSize     = num("Size",        10,  6, 24);
    public final Setting<Integer>       oversample    = num("Oversample",   4,  2,  8);

    public final Setting<Float>         shiftX        = num("ShiftX",  0.0f, -5.0f, 5.0f);
    public final Setting<Float>         shiftY        = num("ShiftY",  1.0f, -5.0f, 5.0f);

    public final Setting<AntialiasMode> antialiasMode = mode("Antialias", AntialiasMode.LIGHT);
    public final Setting<Boolean>       autoHint      = bool("AutoHint",   false);

    public final Setting<Integer>       shadowDarken  = num("ShadowDarken", 70, 60, 85);

    public FontModule() {
        super("Font", "TrueType font renderer for HUD elements.", Category.CLIENT);
    }

    public enum AntialiasMode {
        NORMAL,
        LIGHT
    }
}
