package org.comroid.cmdr;

import com.google.common.flogger.FluentLogger;
import org.comroid.api.Rewrapper;
import org.comroid.api.ValueType;
import org.comroid.cmdr.model.Cmdr;
import org.comroid.cmdr.model.Command;
import org.comroid.cmdr.model.CommandBlob;
import org.comroid.cmdr.model.CommandParameter;
import org.comroid.util.Bitmask;
import org.comroid.util.StandardValueType;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.comroid.util.FallbackUtil.fallback;

public class CommandManager implements Cmdr {
    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    protected final Map<String, CommandBlob> cmds = new ConcurrentHashMap<>();

    @Override
    public final Set<CommandBlob> registerCommands(Class<?>... cls) {
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

    public final boolean executeCommand(Cmdr cmdr, String[] cmdParts, Object[] extraArgs) {
        Object response = null;
        try {
            response = stepIntoCommand(cmdParts, extraArgs);
        } catch (Throwable t) {
            response = cmdr.handleThrowable(t);
            return false;
        } finally {
            cmdr.handleResponse(response, extraArgs);
        }
        return true;
    }

    private Object stepIntoCommand(String[] cmdParts, Object[] extraArgs) {
        CommandBlob blob;
        int[] i = new int[]{0};
        blob = cmds.get(cmdParts[i[0]]);
        while (cmdParts.length > ++i[0] && blob.getSubCommands()
                .stream()
                .flatMap(CommandBlob::names)
                .anyMatch(x -> x.equals(cmdParts[i[0]])))
            blob = blob.getSubCommands()
                    .stream()
                    .filter(x -> x.names().anyMatch(y -> y.equals(cmdParts[i[0]])))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
        return runCommand(blob, Arrays.copyOfRange(cmdParts, i[0], cmdParts.length), extraArgs);
    }

    private Object runCommand(CommandBlob commandBlob, String[] args, Object[] extraArgs) {
        final List<Object> pArgs = new ArrayList<>();
        final List<CommandParameter<?>> params = commandBlob.getParameters();
        long n;
        if ((n = params.stream().filter(x -> x.required).count()) < args.length)
            throw new CommandException("Invalid argument count " + args.length + "; expected " + n);
        for (int i = 0; i < args.length; i++)
            pArgs.add(StandardValueType.STRING.convert(args[i], params.get(i).type));
        return commandBlob.getDelegate().autoInvoke(Stream.concat(pArgs.stream(), Stream.of(extraArgs)));
    }

    @Override
    public Stream<Object> getExtraArguments() {
        return Stream.empty();
    }

    @Override
    public Object handleThrowable(Throwable t) {
        throw new RuntimeException("Unhandled internal Exception", t);
    }

    @Override
    public void handleResponse(Object o, Object[] extraArgs) {
        log.at(Level.SEVERE).log("Unhandled response: {0}", o);
    }
}
