package org.comroid.cmdr;

import org.comroid.cmdr.model.Cmdr;
import org.comroid.cmdr.model.Command;
import org.comroid.cmdr.model.CommandBlob;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandManager implements Cmdr {
    protected final Map<String, CommandBlob> cmds = new ConcurrentHashMap<>();

    @Override
    public Set<CommandBlob> register(Class<?> cls) {
        return Stream.of(cls)
                .map(this::buildCommandGroup)
                .peek(cmd -> cmd.names().peek(key -> {
                    if (cmds.containsKey(key))
                        throw new RuntimeException("Duplicate alias: " + key);
                }).forEach(key -> cmds.put(key, cmd)))
                .collect(Collectors.toSet());
    }

    private CommandBlob buildCommandGroup(Class<?> cls) {
        Arrays.stream(cls.getMethods())
                .filter(mtd -> (mtd.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) != 0)
                .filter(mtd -> mtd.isAnnotationPresent(Command.class))
                .map(this::buildCommandBlob)
    }

    private CommandBlob buildCommandBlob(Method method) {
        Command cmd = method.getAnnotation(Command.class);
        Command.Alias alias = method.getAnnotation(Command.Alias.class);
        Command.Group group = method.getAnnotation(Command.Group.class);
        Command.Hidden hidden = method.getAnnotation(Command.Hidden.class);
        return new CommandBlob(
                method,
                cmd.name(),
                cmd.description().isEmpty() ? null : cmd.description(),
                alias == null ? new String[0] : alias.value(),
                group == null ? null : group.value(),
                hidden != null,
                subCommands) {
        };
    }

    @Override
    public Map<String, CommandBlob> getCommands() {
        return Collections.unmodifiableMap(cmds);
    }
}
