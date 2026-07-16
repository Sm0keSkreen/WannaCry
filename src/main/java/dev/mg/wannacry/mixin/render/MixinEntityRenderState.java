package dev.mg.wannacry.mixin.render;

import dev.mg.wannacry.ducks.render.IEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class MixinEntityRenderState implements IEntityRenderState {
    @Unique
    private @Nullable Entity satellite$entity;

    @Override
    public @Nullable Entity satellite$getEntity() {
        return satellite$entity;
    }

    @Override
    public void satellite$setEntity(Entity entity) {
        this.satellite$entity = entity;
    }
}
