package dev.mg.wannacry.mixin.render;

import dev.mg.wannacry.features.modules.render.NoRenderModule;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class MixinNoRenderGlowing {

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true, require = 0)
    private void satellite$noGlowing(CallbackInfoReturnable<Boolean> cir) {
        NoRenderModule nr = NoRenderModule.getInstance();
        if (nr != null && nr.isEnabled() && nr.glowing.getValue()) cir.setReturnValue(false);
    }
}
