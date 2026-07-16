package dev.mg.wannacry.event.impl.entity.player;

import dev.mg.wannacry.event.Event;
import dev.mg.wannacry.event.Stage;

public class UpdateWalkingPlayerEvent extends Event {
    private final Stage stage;

    public UpdateWalkingPlayerEvent(Stage stage) {
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }
}
