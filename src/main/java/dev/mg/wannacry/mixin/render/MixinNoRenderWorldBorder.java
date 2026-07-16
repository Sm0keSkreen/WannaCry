package dev.mg.wannacry.mixin.render;

import dev.mg.wannacry.features.modules.render.NoRenderModule;
import net.minecraft.client.renderer.WorldBorderRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorderRenderer.class)
public class MixinNoRenderWorldBorder {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true, require = 0)
    private void satellite$noWorldBorder(CallbackInfo ci) {
        NoRenderModule nr = NoRenderModule.getInstance();
        if (nr != null && nr.isEnabled() && nr.worldBorder.getValue()) ci.cancel();
    }
}
