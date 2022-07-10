package org.comroid.cmdr.model;

import org.comroid.api.Invocable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("ClassExplicitlyAnnotation")
public class CommandBlob implements Command {
    private final @Nullable Invocable<?> delegate;
    private final @NotNull String name;
    private final @Nullable String description;
    private final @NotNull @NonNls String[] aliases;
    private final boolean hidden;
    private final Collection<CommandBlob> subCommands;
    private final List<CommandParameter<?>> parameters;

    public CommandBlob(
            @Nullable Method method,
            @NotNull String name,
            @Nullable String description,
            @NotNull @NonNls String[] aliases,
            boolean hidden,
            Collection<CommandBlob> subCommands,
            CommandParameter<?>... parameters
    ) {
        this.delegate = method != null ? Invocable.ofMethodCall(method) : null;
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.hidden = hidden;
        this.subCommands = subCommands;
        this.parameters = Arrays.asList(parameters);
    }

    public @Nullable Invocable<?> getDelegate() {
        return delegate;
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
    
    public boolean hidden() {
        return hidden;
    }

    public Collection<CommandBlob> getSubCommands() {
        return subCommands;
    }

    public List<CommandParameter<?>> getParameters() {
        return parameters;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Command.class;
    }

    public Collection<String> autoCompleteOptions(Cmdr cmdr, String[] args, Object... extraArgs) {
        return Collections.emptyList(); // todo
    }

    public Object evaluate(Cmdr cmdr, String[] args, Object... extraArgs) {
        return null; // todo
    }
}
