package dev.mg.wannacry.ducks.render;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public interface IEntityRenderState {
    @Nullable Entity satellite$getEntity();
    void satellite$setEntity(Entity entity);
}
