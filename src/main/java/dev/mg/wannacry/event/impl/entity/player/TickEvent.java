package dev.mg.wannacry.event.impl.entity.player;

import dev.mg.wannacry.event.Event;

public class TickEvent extends Event {
    public static class Post extends TickEvent { }

    public static class Pre extends TickEvent { }
}
