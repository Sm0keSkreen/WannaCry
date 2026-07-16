package dev.mg.wannacry.features.commands.argument;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.Feature;
import dev.mg.wannacry.features.commands.CommandExceptions;
import dev.mg.wannacry.features.modules.Module;

import java.util.concurrent.CompletableFuture;

import static dev.mg.wannacry.features.commands.ArgumentSuggestions.suggest;

public record ModuleArgumentType(boolean fullName) implements ArgumentType<Module> {
    @Override
    public Module parse(StringReader reader) throws CommandSyntaxException {
        String value = reader.readString().toLowerCase();

        for (Module module : WannaCry.moduleManager.getModules()) {
            if (value.equalsIgnoreCase(module.getName()) || module.getName().startsWith(value)) {
                return module;
            }
        }

        throw CommandExceptions.invalidArgument("Invalid module").createWithContext(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return suggest(WannaCry.moduleManager.getModules(), Feature::getName, builder);
    }

    public static ModuleArgumentType module() {
        return module(false);
    }

    public static ModuleArgumentType module(boolean fullName) {
        return new ModuleArgumentType(fullName);
    }

    public static Module getModule(CommandContext<?> ctx, String name) {
        return ctx.getArgument(name, Module.class);
    }
}
