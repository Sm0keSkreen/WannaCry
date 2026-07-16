package dev.mg.wannacry.mixin.render;

import dev.mg.wannacry.features.modules.render.FullbrightModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(OptionInstance.class)
public class MixinOptionInstance<T> {

    @SuppressWarnings("unchecked")
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void satellite$overrideGammaForFullbright(CallbackInfoReturnable<T> cir) {
        FullbrightModule fb = FullbrightModule.getInstance();
        if (fb == null || !fb.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.options == null) return;

        if ((Object) this != mc.options.gamma()) return;

        cir.setReturnValue((T) (Double) 15.0);
    }
}
