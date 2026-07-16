package dev.mg.wannacry.mixin.input;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface IMinecraft {

    @Invoker("startAttack")
    boolean invokeStartAttack();
}
