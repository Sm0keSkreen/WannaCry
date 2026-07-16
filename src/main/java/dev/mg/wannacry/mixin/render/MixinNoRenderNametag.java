package dev.mg.wannacry.mixin.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.mg.wannacry.features.modules.render.NoRenderModule;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinNoRenderNametag<T, S extends EntityRenderState> {

    @Inject(method = "renderNameTagIfPresent", at = @At("HEAD"), cancellable = true, require = 0)
    private void satellite$noNametag(
            S state,
            Component displayName,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            CallbackInfo ci) {
        NoRenderModule nr = NoRenderModule.getInstance();
        if (nr != null && nr.isEnabled() && nr.nametags.getValue()) ci.cancel();
    }
}
