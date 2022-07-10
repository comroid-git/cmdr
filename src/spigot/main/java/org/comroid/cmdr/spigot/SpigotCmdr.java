package org.comroid.cmdr.spigot;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.comroid.cmdr.CommandManager;
import org.comroid.cmdr.model.Cmdr;
import org.comroid.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SpigotCmdr extends JavaPlugin implements Cmdr.Underlying {
    private final CommandManager cmdr = new CommandManager();

    @Override
    public final Cmdr getUnderlyingCmdr() {
        return cmdr;
    }

    @Override
    public Stream<Object> getExtraArguments() {
        return Stream.of(this);
    }

    @Override
    public final List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String alias, @NotNull String[] args) {
        return cmdr.autoComplete(getCommands().get(alias), args, Stream.concat(getExtraArguments(), Stream.of(sender)).toArray()).collect(Collectors.toList());
    }

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        return cmdr.executeCommand(
                this,
                Stream.concat(Stream.of(label), Stream.of(args)).toArray(String[]::new),
                Stream.concat(getExtraArguments(), Stream.of(sender)).toArray()
        );
    }

    @Override
    public void handleResponse(Object response, Object[] extraArgs) {
        CommandSender sender = ArrayUtil.get(extraArgs, CommandSender.class);
        sender.sendMessage(String.valueOf(response));
    }
}
