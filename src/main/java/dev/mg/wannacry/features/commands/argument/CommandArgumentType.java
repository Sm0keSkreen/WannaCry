package dev.mg.wannacry.features.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.commands.Command;
import dev.mg.wannacry.features.commands.CommandExceptions;

import java.util.concurrent.CompletableFuture;

import static dev.mg.wannacry.features.commands.ArgumentSuggestions.suggest;

public final class CommandArgumentType implements ArgumentType<Command>, CommandExceptionType {
    @Override
    public Command parse(StringReader reader) throws CommandSyntaxException {
        String value = reader.readString().toLowerCase();

        for (String alias : WannaCry.commandManager.getCommandAliases()) {
            if (value.equalsIgnoreCase(alias)) {
                return WannaCry.commandManager.getCommand(alias);
            }
        }

        throw CommandExceptions.invalidArgument("Invalid command").createWithContext(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return suggest(WannaCry.commandManager.getCommandAliases(), builder);
    }

    public static Command getCommand(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, Command.class);
    }

    public static CommandArgumentType command() {
        return new CommandArgumentType();
    }
}
