package dev.mg.wannacry.features.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.mg.wannacry.features.commands.Command;
import dev.mg.wannacry.features.modules.Module;
import dev.mg.wannacry.manager.CommandManager;

import static dev.mg.wannacry.features.commands.argument.ModuleArgumentType.getModule;
import static dev.mg.wannacry.features.commands.argument.ModuleArgumentType.module;

public class ToggleCommand extends Command {
    public ToggleCommand() {
        super("toggle", "t");
        setDescription("Toggles a module");
    }

    @Override
    public void createArgumentBuilder(LiteralArgumentBuilder<CommandManager> builder) {
        builder.then(argument("module", module(true))
                .executes((ctx) -> {
                    Module module = getModule(ctx, "module");
                    module.toggle();
                    boolean toggled = module.isEnabled();
                    return success("{gray} %s {reset} is now %s %s",
                            module.getDisplayName(),
                            toggled ? "{green}" : "{red}",
                            toggled ? "enabled" : "disabled");
                }));
    }
}
