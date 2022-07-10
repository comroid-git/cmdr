package org.comroid.cmdr;

import com.google.common.flogger.FluentLogger;
import org.comroid.api.Rewrapper;
import org.comroid.api.ValueType;
import org.comroid.cmdr.model.Cmdr;
import org.comroid.cmdr.model.Command;
import org.comroid.cmdr.model.CommandBlob;
import org.comroid.cmdr.model.CommandParameter;
import org.comroid.util.Bitmask;
import org.comroid.util.FallbackUtil;
import org.comroid.util.StandardValueType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.comroid.util.FallbackUtil.*;

public class CommandManager implements Cmdr {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    protected final Map<String, CommandBlob> cmds = new ConcurrentHashMap<>();

    @Override
    public final Set<CommandBlob> registerCommand(Class<?> cls) {
        return Stream.of(cls)
                .map(this::buildCommandGroup)
                .peek(cmd -> cmd.names().peek(key -> {
                    if (cmds.containsKey(key))
                        throw new RuntimeException("Duplicate alias: " + key);
                }).forEach(key -> cmds.put(key, cmd)))
                .collect(Collectors.toSet());
    }

    private CommandBlob buildCommandGroup(Class<?> cls) {
        List<CommandBlob> subcmds = Arrays.stream(cls.getMethods())
                .filter(mtd -> Bitmask.isFlagSet(mtd.getModifiers(), (Modifier.PUBLIC | Modifier.STATIC)))
                .filter(mtd -> mtd.isAnnotationPresent(Command.class))
                .map(this::buildCommandBlob)
                .collect(Collectors.toList());
        Command cmd = cls.getAnnotation(Command.class);
        Command.Alias alias = cls.getAnnotation(Command.Alias.class);
        Command.Hidden hidden = cls.getAnnotation(Command.Hidden.class);
        return new CommandBlob(
                null,
                fallback(cmd.name(), cls::getSimpleName, String::isEmpty, Objects::isNull),
                fallback(cmd.description(), Rewrapper.empty(), String::isEmpty, Objects::isNull),
                alias == null ? new String[0] : alias.value(),
                hidden != null,
                subcmds
        );
    }

    private CommandBlob buildCommandBlob(Method mtd) {
        Command cmd = mtd.getAnnotation(Command.class);
        Command.Alias alias = mtd.getAnnotation(Command.Alias.class);
        Command.Hidden hidden = mtd.getAnnotation(Command.Hidden.class);
        List<CommandParameter<?>> params = new ArrayList<>();
        for (Parameter parameter : mtd.getParameters()) {
            if (!parameter.isAnnotationPresent(Command.Arg.class))
                continue;
            Command.Arg arg = parameter.getAnnotation(Command.Arg.class);
            Class<?> type = parameter.getType();
            ValueType<?> vType = StandardValueType.forClass(type).assertion("Non-standard type not supported: " + type);
            params.add(new CommandParameter<>(vType, parameter.getName(), arg.ordinal(), arg.value()));
        }
        params.sort(Comparator.comparingInt(it -> it.ordinal));

        return new CommandBlob(
                mtd,
                fallback(cmd.name(), mtd::getName, String::isEmpty, Objects::isNull),
                fallback(cmd.description(), Rewrapper.empty(), String::isEmpty, Objects::isNull),
                alias == null ? new String[0] : alias.value(),
                hidden != null,
                Collections.emptyList(),
                params.toArray(new CommandParameter[0]));
    }

    @Override
    public final Map<String, CommandBlob> getCommands() {
        return Collections.unmodifiableMap(cmds);
    }

    public final Stream<String> autoComplete(CommandBlob commandBlob, String[] args, Object[] extraArgs) {
        return Stream.concat(commandBlob.getSubCommands().stream().flatMap(CommandBlob::names),
                commandBlob.autoCompleteOptions(this, args, extraArgs).stream());
    }

    public final Object runCommand(CommandBlob commandBlob, String[] args, Object[] extraArgs) {
        final List<Object> pArgs = new ArrayList<>();
        final List<CommandParameter<?>> params = commandBlob.getParameters();
        for (int i = 0; i < args.length; i++) {
            pArgs.add(StandardValueType.STRING.convert(args[0], params.get(i).type));
        }
        return commandBlob.getDelegate().autoInvoke(Stream.concat(pArgs.stream(), Stream.of(extraArgs)));
    }

    @Override
    public Stream<Object> getExtraArguments() {
        return Stream.empty();
    }

    @Override
    public Object handleThrowable(Throwable t) {
        return String.format("%s: %s", t.getClass().getSimpleName(), t.getMessage());
    }

    @Override
    public void handleResponse(Object o) {
        log.at(Level.FINE).log("Unhandled response: {}", o);
    }
}
