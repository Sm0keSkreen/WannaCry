package dev.mg.wannacry.features.modules.movement;

import dev.mg.wannacry.event.impl.entity.player.TickEvent;
import dev.mg.wannacry.event.system.Subscribe;
import dev.mg.wannacry.features.modules.Module;
import net.minecraft.world.phys.Vec2;

public class AutoSprintModule extends Module {
    public AutoSprintModule() {
        super("Sprint", "Automatically sprints for you.", Category.CLOSET);
    }

    @Subscribe
    public void onPreTick(TickEvent.Pre event) {
        if (nullCheck()) return;

        Vec2 move = mc.player.input.getMoveVector();
        if (move.y != 0 && !mc.player.isSprinting() && !mc.player.isInWater()
                && !mc.player.isUnderWater() && !mc.player.isCrouching()
                && mc.player.getFoodData().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }

    @Override
    public void onDisable() {
        if (nullCheck()) return;
        mc.player.setSprinting(false);
    }
}
