package dev.mg.wannacry.event.impl.input;

import dev.mg.wannacry.event.Event;
import net.minecraft.world.entity.Entity;

public class AttackEntityEvent extends Event {
    private final Entity entity;

    public AttackEntityEvent(Entity entity) {
        this.entity = entity;
    }

    public Entity getEntity() {
        return entity;
    }
}
