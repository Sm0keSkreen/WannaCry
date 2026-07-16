package dev.mg.wannacry.features.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.mg.wannacry.features.commands.Command;
import dev.mg.wannacry.manager.CommandManager;

public class TriangulationResetCommand extends Command {

    public TriangulationResetCommand() {
        super("triangulationreset", "treset");
        setDescription("Clears all stronghold triangulation samples and resets the prediction");
    }

    @Override
    public void createArgumentBuilder(LiteralArgumentBuilder<CommandManager> builder) {
        builder.executes(ctx -> fail("StrongholdTriangulation module is not installed."));
    }
}
