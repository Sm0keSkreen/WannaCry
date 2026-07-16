package dev.mg.wannacry.mixin.render;

import dev.mg.wannacry.features.modules.render.NoRenderModule;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class MixinNoRenderWeather {

    @Inject(method = "renderSnowAndRain", at = @At("HEAD"), cancellable = true, require = 0)
    private void satellite$noWeather(
            LightTexture lightTexture,
            float partialTick,
            double camX, double camY, double camZ,
            CallbackInfo ci) {
        NoRenderModule nr = NoRenderModule.getInstance();
        if (nr != null && nr.isEnabled() && nr.weather.getValue()) ci.cancel();
    }
}
