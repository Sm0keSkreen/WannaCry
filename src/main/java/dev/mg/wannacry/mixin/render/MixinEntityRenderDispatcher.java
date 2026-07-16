package dev.mg.wannacry.mixin.render;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.mg.wannacry.ducks.render.IEntityRenderState;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class MixinEntityRenderDispatcher {

    @ModifyExpressionValue(
        method = "extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
        at = @At(value = "INVOKE",
                 target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;createRenderState(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;")
    )
    private <E extends Entity> EntityRenderState satellite$setEntity(EntityRenderState state, E entity, float partialTicks) {
        ((IEntityRenderState) state).satellite$setEntity(entity);
        return state;
    }
}
