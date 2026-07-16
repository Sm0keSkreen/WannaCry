package dev.mg.wannacry.mixin.render;

import dev.mg.wannacry.features.modules.render.NoRenderModule;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class MixinNoRenderLiving<T extends LivingEntity, S extends LivingEntityRenderState> {

    @Inject(
        method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;" +
                 "Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
        at = @At("TAIL"),
        require = 0
    )
    private void satellite$noDeadEntities(T entity, S state, float partialTick, CallbackInfo ci) {
        NoRenderModule nr = NoRenderModule.getInstance();
        if (nr != null && nr.isEnabled() && nr.deadEntities.getValue() && entity.deathTime > 0) {
            state.deathTime = 0f;
        }
    }
}
