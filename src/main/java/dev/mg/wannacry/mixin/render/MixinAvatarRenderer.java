package dev.mg.wannacry.mixin.render;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.util.models.Angles;
import net.minecraft.client.CameraType;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.mg.wannacry.util.traits.Util.mc;

@Mixin(AvatarRenderer.class)
public class MixinAvatarRenderer {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private <T extends Avatar> void extractRenderStateHook(T avatar, AvatarRenderState state, float f, CallbackInfo ci) {
        if (mc.player != avatar) return;

        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;

        Angles lerped = WannaCry.rotationManager.getLerpRenderSnapshot(f);
        if (lerped == null) return;
        state.xRot = lerped.xRot();
        state.yRot = 0;
        state.bodyRot = lerped.yRot();
    }
}
