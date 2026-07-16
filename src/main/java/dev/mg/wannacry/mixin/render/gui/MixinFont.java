package dev.mg.wannacry.mixin.render.gui;

import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Font.class)
public interface MixinFont {
    @Accessor("provider")
    Font.Provider getProvider();
}
