package dev.mg.wannacry.mixin.input;

import dev.mg.wannacry.event.impl.input.AttackEvent;
import dev.mg.wannacry.event.impl.input.UseItemEvent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.mg.wannacry.util.traits.Util.EVENT_BUS;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void onStartAttackPre(CallbackInfoReturnable<Boolean> cir) {
        EVENT_BUS.post(new AttackEvent.Pre());
    }

    @Inject(method = "startAttack", at = @At("TAIL"))
    private void onStartAttackPost(CallbackInfoReturnable<Boolean> cir) {
        EVENT_BUS.post(new AttackEvent.Post());
    }

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void onStartUseItem(CallbackInfo ci) {
        EVENT_BUS.post(new UseItemEvent());
    }
}
