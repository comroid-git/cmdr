package org.comroid.cmdr.model;

import org.comroid.api.Invocable;
import org.comroid.api.Rewrapper;
import org.comroid.cmdr.CommandManager;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
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

    public @Nullable Invocable<?> getDelegate() {
        return delegate;
    }

    public Rewrapper<CommandBlob> getDefaultCmd() {
        return Rewrapper.ofOptional(subCommands.stream().filter(x -> x.names().anyMatch(y -> y.equals(defaultCmd))).findFirst());
    }

    public Collection<CommandBlob> getSubCommands() {
        return subCommands;
    }

    public List<CommandParameter<?>> getParameters() {
        return parameters;
    }

    public CommandBlob(
            @Nullable Object target,
            @Nullable Method method,
            @NotNull String name,
            @Nullable String description,
            @NotNull @NonNls String[] aliases,
            @Nullable String defaultCmd,
            boolean hidden,
            Collection<CommandBlob> subCommands,
            CommandParameter<?>... parameters
    ) {
        this.delegate = method != null ? Invocable.ofMethodCall(target, method) : null;
        this.name = name;
        this.description = description;
        this.aliases = aliases;
        this.defaultCmd = defaultCmd;
        this.hidden = hidden;
        this.subCommands = subCommands;
        this.parameters = Arrays.asList(parameters);
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

    @Override
    public Class<? extends Annotation> annotationType() {
        return Command.class;
    }

    public Stream<String> autoCompleteOptions(Cmdr cmdr, String[] cmdParts, Object[] extraArgs) {
        CommandBlob cmd;
        int[] i = new int[]{0};
        cmd = cmdr.getCommands().get(cmdParts[i[0]]);
        cmd = CommandManager.extractCommandBlob(cmd, cmdParts, i);
        if (cmdParts.length - 1 - i[0] < cmd.getParameters().stream().filter(x -> x.required).count() && cmdParts.length - 1 - i[0] >= 0)
            return Arrays.stream(cmd.parameters.get(cmdParts.length - 1 - i[0]).autoCompleteOptions)
                    .map(cmdr::prefixAutofillOption);
        return Stream.empty();
    }

    @Override
    public String toString() {
        return name + " - " + Objects.requireNonNullElse(description, "No description provided");
    }
}
