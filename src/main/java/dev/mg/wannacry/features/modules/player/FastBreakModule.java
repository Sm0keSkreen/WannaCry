package dev.mg.wannacry.features.modules.player;

import dev.mg.wannacry.event.impl.entity.player.TickEvent;
import dev.mg.wannacry.event.system.Subscribe;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.features.settings.Setting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Random;

public class FastBreakModule extends Module {

    private final Setting<Float> activationChance = num(
            "ActivationChance", 1.0f, 0.0f, 1.0f);

    private final Setting<Boolean> legitMode = bool(
            "LegitMode", false);

    private final Random random = new Random();

    private BlockPos lastBlockPos = null;

    private boolean fastBreakBlock = false;

    public FastBreakModule() {
        super("FastBreak", "Mine blocks faster and removes the delay between breaks.", Category.CLOSET);
        legitMode.setVisibility(v -> true);
        activationChance.setVisibility(v -> !legitMode.getValue());
    }

    @Subscribe
    public void onPreTick(TickEvent.Pre event) {
        if (nullCheck()) return;

        mc.gameMode.destroyDelay = 0;

        if (legitMode.getValue()) return;

        if (!(mc.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK) {
            lastBlockPos = null;
            return;
        }

        BlockPos blockPos = hit.getBlockPos();

        if (!blockPos.equals(lastBlockPos)) {
            lastBlockPos = blockPos;
            fastBreakBlock = random.nextFloat() <= activationChance.getValue();
        }

        if (!fastBreakBlock) return;

        BlockState state = mc.level.getBlockState(blockPos);
        if (state.getDestroySpeed(mc.level, blockPos) < 0) return;

        if (mc.gameMode.destroyProgress <= 0) return;

        if (mc.gameMode.destroyProgress >= 1) return;

        Direction direction = hit.getDirection();
        mc.player.connection.send(
                new ServerboundPlayerActionPacket(
                        ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                        blockPos,
                        direction,
                        (int) mc.player.level().getGameTime()));
    }

    @Override
    public void onDisable() {
        lastBlockPos = null;
    }

    @Override
    public String getDisplayInfo() {
        return legitMode.getValue() ? "Legit" : null;
    }
}
