package org.comroid.cmdr.model;

import org.comroid.api.Invocable;
import org.comroid.api.Rewrapper;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings("ClassExplicitlyAnnotation")
public class CommandBlob implements Command {
    private final @Nullable Invocable<?> delegate;
    private final @NotNull String name;
    private final @Nullable String description;
    private final @NotNull @NonNls String[] aliases;
    private final @Nullable String defaultCmd;
    private final boolean hidden;
    private final Collection<CommandBlob> subCommands;
    private final List<CommandParameter<?>> parameters;

    public CommandBlob(
            @Nullable Method method,
            @NotNull String name,
            @Nullable String description,
            @NotNull @NonNls String[] aliases,
            @Nullable String defaultCmd,
            boolean hidden,
            Collection<CommandBlob> subCommands,
            CommandParameter<?>... parameters
    ) {
        this.delegate = method != null ? Invocable.ofMethodCall(method) : null;
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.defaultCmd = defaultCmd;
        this.hidden = hidden;
        this.subCommands = subCommands;
        this.parameters = Arrays.asList(parameters);
    }

    public @Nullable Invocable<?> getDelegate() {
        return delegate;
    }

    public Rewrapper<CommandBlob> getDefaultCmd() {
        return Rewrapper.ofOptional(subCommands.stream().filter(x -> x.names().anyMatch(y -> y.equals(defaultCmd))).findFirst());
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

    public Collection<String> autoCompleteOptions(Cmdr cmdr, String[] cmdParts, Object[] extraArgs) {
        CommandBlob cmd;
        int[] i = new int[]{0};
        cmd = cmdr.getCommands().get(cmdParts[i[0]]);
        while (cmdParts.length > ++i[0] && cmd.getSubCommands()
                .stream()
                .flatMap(CommandBlob::names)
                .anyMatch(x -> x.equals(cmdParts[i[0]])))
            cmd = cmd.getSubCommands()
                    .stream()
                    .filter(x -> x.names().anyMatch(y -> y.equals(cmdParts[i[0]])))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
        if (cmdParts.length-1-i[0] < cmd.getParameters().stream().filter(x -> x.required).count()-1)
            return Arrays.asList(cmdr.handleInvalidArguments(cmd, Arrays.copyOfRange(cmdParts, i[0], cmdParts.length)).toString());
        return new ArrayList<>();
    }
}
