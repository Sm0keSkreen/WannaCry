package dev.mg.wannacry.mixin.input;

import dev.mg.wannacry.event.impl.input.AttackEntityEvent;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.mg.wannacry.util.traits.Util.EVENT_BUS;

@Mixin(MultiPlayerGameMode.class)
public class MixinMultiPlayerGameMode {

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Player player, Entity target, CallbackInfo ci) {
        if (EVENT_BUS.post(new AttackEntityEvent(target))) {
            ci.cancel();
        }
    }
}
