package dev.mg.wannacry.features.modules.render;

import dev.mg.wannacry.features.modules.Module;

public class FullbrightModule extends Module {

    private static FullbrightModule INSTANCE;

    public FullbrightModule() {
        super("Fullbright", "sets really high gamma", Category.RENDER);
        INSTANCE = this;
    }

    public static FullbrightModule getInstance() {
        return INSTANCE;
    }
}
