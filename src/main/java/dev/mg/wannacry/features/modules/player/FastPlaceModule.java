package dev.mg.wannacry.features.modules.player;

import dev.mg.wannacry.event.impl.entity.player.TickEvent;
import dev.mg.wannacry.event.system.Subscribe;
import dev.mg.wannacry.features.modules.Module;

public class FastPlaceModule extends Module {

    public FastPlaceModule() {
        super("FastPlace", "Removes the delay between placing blocks and using items.", Category.CLOSET);
    }

    @Subscribe
    public void onPreTick(TickEvent.Pre event) {
        if (nullCheck()) return;
        mc.rightClickDelay = 0;
    }
}
