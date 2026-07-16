package dev.mg.wannacry.event.impl.input;

import dev.mg.wannacry.event.Event;

public class AttackEvent extends Event {
    public static class Pre  extends AttackEvent {}
    public static class Post extends AttackEvent {}
}
