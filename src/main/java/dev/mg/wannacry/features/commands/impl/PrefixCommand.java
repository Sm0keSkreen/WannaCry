package dev.mg.wannacry.features.commands.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.mg.wannacry.features.commands.Command;
import dev.mg.wannacry.manager.CommandManager;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;

public class PrefixCommand extends Command {
    public PrefixCommand() {
        super("prefix", "setprefix");
        setDescription("Sets the command prefix");
    }

    @Override
    public void createArgumentBuilder(LiteralArgumentBuilder<CommandManager> builder) {
        builder.then(argument("prefix", word())
                .executes((ctx) -> {
                    String prefix = getString(ctx, "prefix");
                    if (prefix == null || prefix.isEmpty()) {
                        return fail("Prefix must contain more than one character");
                    }
                    ctx.getSource().setCommandPrefix(prefix);
                    return success("Prefix changed to {green} %s", prefix);
                }));
    }
}
