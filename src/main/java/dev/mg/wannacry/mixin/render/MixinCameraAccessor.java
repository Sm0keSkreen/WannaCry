package dev.mg.wannacry.mixin.render;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface MixinCameraAccessor {
    @Invoker("setRotation")
    void callSetRotation(float yaw, float pitch);
}
