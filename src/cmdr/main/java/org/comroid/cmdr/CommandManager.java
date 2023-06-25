package org.comroid.cmdr;

import com.google.common.flogger.FluentLogger;
import org.comroid.api.Invocable;
import org.comroid.api.Rewrapper;
import org.comroid.api.ValueType;
import org.comroid.cmdr.model.*;
import org.comroid.util.Bitmask;
import org.comroid.util.StandardValueType;
import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

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
    private final @Nullable CommandHandler handler;

    public CommandManager() {
        this(null);
    }

    public CommandManager(@Nullable CommandHandler handler) {
        this.handler = handler;
    }

    @Override
    public final Map<String, CommandBlob> getCommands() {
        return Collections.unmodifiableMap(cmds);
    }

    @Override
    public Stream<Object> getExtraArguments() {
        return Stream.empty();
    }

    @Internal
    public static CommandBlob extractCommandBlob(CommandBlob base, String[] cmdParts, int[] partIndex) {
        while (cmdParts.length > ++partIndex[0] && base.getSubCommands()
                .stream()
                .flatMap(CommandBlob::names)
                .anyMatch(x -> x.equals(cmdParts[partIndex[0]])))
            base = base.getSubCommands()
                    .stream()
                    .filter(x -> x.names().anyMatch(y -> y.equals(cmdParts[partIndex[0]])))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);
        return base;
    }

    @Override
    public final Set<CommandBlob> registerCommands(Class<?>... cls) {
        return Stream.of(cls)
                .flatMap(kls -> kls.isAnnotationPresent(Command.class)
                        ? Stream.of(buildCommandGroup(kls))
                        : Stream.of(kls.getDeclaredMethods())
                        .filter(mtd -> mtd.isAnnotationPresent(Command.class))
                        .map(this::buildCommandBlob))
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
        Command.Default defaultCmd = cls.getAnnotation(Command.Default.class);
        return new CommandBlob(
                null,
                fallback(cmd.name(), cls::getSimpleName, String::isEmpty, Objects::isNull),
                fallback(cmd.description(), Rewrapper.empty(), String::isEmpty, Objects::isNull),
                alias == null ? new String[0] : alias.value(),
                defaultCmd == null ? null : defaultCmd.value(),
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
            params.add(new CommandParameter<>(vType, parameter.getName(), arg.ordinal(), arg.required(), arg.autoComplete()));
        }
        params.sort(Comparator.comparingInt(it -> it.ordinal));

        return new CommandBlob(
                mtd,
                fallback(cmd.name(), mtd::getName, String::isEmpty, Objects::isNull),
                fallback(cmd.description(), Rewrapper.empty(), String::isEmpty, Objects::isNull),
                alias == null ? new String[0] : alias.value(),
                null,
                hidden != null,
                Collections.emptyList(),
                params.toArray(new CommandParameter[0]));
    }

    public final Stream<String> autoComplete(Cmdr cmdr, CommandBlob commandBlob, String[] cmdParts, Object[] extraArgs) {
        commandBlob = extractCommandBlob(commandBlob, cmdParts, new int[]{0});
        return Stream.concat(
                commandBlob.getSubCommands().stream().flatMap(CommandBlob::names),
                commandBlob.autoCompleteOptions(cmdr, cmdParts, extraArgs));
    }

    public final boolean executeCommand(Cmdr cmdr, String[] cmdParts, Object[] extraArgs) {
        Object response = null;
        try {
            response = stepIntoCommand(cmdr, cmdParts, extraArgs);
        } catch (Throwable t) {
            response = cmdr.handleThrowable(cmdParts, Invocable.unwrapInvocationTargetException(t));
            return false;
        } finally {
            if (response != null)
                cmdr.handleResponse(response, extraArgs);
        }
        return true;
    }

    @Override
    public final boolean handle(String cmd, Object... extraArgs) {
        return executeCommand(this, cmd.split(" "),
                Stream.concat(getExtraArguments(), Stream.of(extraArgs)).toArray());
    }

    private Object stepIntoCommand(Cmdr cmdr, String[] cmdParts, Object[] extraArgs) {
        CommandBlob blob;
        int[] i = new int[]{0};
        blob = cmds.get(cmdParts[i[0]]);
        blob = extractCommandBlob(blob, cmdParts, i);
        if (blob == null)
            return handleInvalidCommand(cmdParts);
        return runCommand(cmdr, blob, Arrays.copyOfRange(cmdParts, i[0], cmdParts.length), extraArgs);
    }

    private Object runCommand(Cmdr cmdr, CommandBlob commandBlob, String[] args, Object[] extraArgs) {
        final List<Object> pArgs = new ArrayList<>();
        final List<CommandParameter<?>> params = commandBlob.getParameters();
        long required = params.stream().filter(x -> x.required).count();
        int argCount = args.length + extraArgs.length;
        if (required > argCount)
            throw new CommandException("Invalid argument count " + argCount + "; expected " + required);
        for (int i = 0; i < args.length; i++)
            pArgs.add(StandardValueType.STRING.convert(args[i], params.get(i).type));
        Invocable<?> delegate = commandBlob.getDelegate();
        if (delegate == null)
            delegate = commandBlob.getDefaultCmd().ifPresentMap(CommandBlob::getDelegate);
        if (delegate == null)
            return "Command '" + commandBlob.name() + "' not executable";
        try {
            return delegate.autoInvoke(Stream.concat(pArgs.stream(), Stream.concat(Stream.of(cmdr), Stream.of(extraArgs))).toArray());
        } catch (IllegalArgumentException t) {
            if (!(t.getCause() instanceof IllegalArgumentException))
                throw t;
            return cmdr.handleInvalidArguments(commandBlob, args);
        }
    }

    @Override
    public Object handleThrowable(String[] parts, Throwable t) {
        if (handler != null)
            return handler.handleThrowable(parts, t);
        throw new RuntimeException("Unhandled internal Exception", t);
    }

    @Override
    public Object handleInvalidCommand(String[] parts) {
        if (handler != null)
            return handler.handleInvalidCommand(parts);
        return "Invalid command: " + String.join(" ", parts);
    }

    @Override
    public Object handleInvalidArguments(CommandBlob cmd, String[] args) {
        if (handler != null)
            return handler.handleInvalidArguments(cmd, args);
        return "Invalid arguments: " + String.join(" ", args);
    }

    @Override
    public void handleResponse(Object o, Object[] extra) {
        if (handler != null)
            handler.handleResponse(o, extra);
        else log.at(Level.FINE).log("Unhandled response: {0}", o);
    }
}
