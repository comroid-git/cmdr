package org.comroid.cmdr.model;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.Stream;

@SuppressWarnings("ClassExplicitlyAnnotation")
public abstract class CommandBlob implements Command {
    private final Method method;
    private final @NotNull String name;
    private final @Nullable String description;
    private final @NotNull @NonNls String[] aliases;
    private final @Nullable String group;
    private final boolean hidden;
    private final Collection<CommandBlob> subCommands;

    protected CommandBlob(
            @NotNull Method method,
            @NotNull String name,
            @Nullable String description,
            @NotNull @NonNls String[] aliases,
            @Nullable String group,
            boolean hidden,
            Collection<CommandBlob> subCommands
    ) {
        this.method = method;
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.group = group;
        this.hidden = hidden;
        this.subCommands = subCommands;
    }

    @Override
    public @NotNull String name() {
        return name;
    }

    @Override
    public @Nullable String description() {
        return description;
    }

    public @NotNull @NonNls String[] aliases() {
        return aliases;
    }

    public @NotNull @NonNls Stream<String> names() {
        return Stream.concat(Stream.of(name), Stream.of(aliases)).distinct();
    }
    
    public @Nullable String group() {
        return group;
    }
    
    public boolean hidden() {
        return hidden;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Command.class;
    }

    public Collection<String> autoComplete(Cmdr cmdr, String[] args) {
        return null; // todo
    }

    public Object evaluate(Cmdr cmdr, String[] args) {
        return null; // todo
    }
}
