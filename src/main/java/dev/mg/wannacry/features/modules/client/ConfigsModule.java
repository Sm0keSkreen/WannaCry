package dev.mg.wannacry.features.modules.client;

import dev.mg.wannacry.features.gui.ConfigsScreen;
import dev.mg.wannacry.features.modules.Module;

public class ConfigsModule extends Module {

    public ConfigsModule() {
        super("Configs", "Save and load client configs", Category.CLIENT);
    }

    @Override
    public void onEnable() {
        if (nullCheck()) {
            disable();
            return;
        }
        mc.setScreen(ConfigsScreen.getInstance());
        disable();
    }
}
