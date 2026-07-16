package dev.mg.wannacry.mixin.render;

import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.modules.combat.AimCorrectionModule;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Camera.class)
public class MixinCameraSetup {

    @Shadow protected void setRotation(float yaw, float pitch) {}

    @Redirect(
        method = "setup",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V")
    )
    public void interceptSetRotation(Camera instance, float yaw, float pitch) {
        AimCorrectionModule mod = WannaCry.moduleManager.getModuleByClass(AimCorrectionModule.class);

        if (mod != null && (mod.isEnabled() || mod.isWindingDown()) && mod.isCameraActive()) {
            ((MixinCameraAccessor) instance).callSetRotation(mod.getCamYaw(), mod.getCamPitch());
        } else {
            this.setRotation(yaw, pitch);
        }
    }
}
