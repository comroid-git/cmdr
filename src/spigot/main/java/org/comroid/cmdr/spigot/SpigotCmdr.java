package org.comroid.cmdr.spigot;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.comroid.cmdr.CommandManager;
import org.comroid.cmdr.model.Cmdr;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpigotCmdr extends JavaPlugin implements Cmdr.Underlying {
    private final CommandManager cmdr = new CommandManager();

    @Override
    public final Cmdr getUnderlyingCmdr() {
        return cmdr;
    }

    @Override
    public final List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String alias, @NotNull String[] args) {
        return cmdr.autoComplete(getCommands().get(alias), args, Stream.concat(getExtraArguments(), Stream.of(this, sender)).toArray()).collect(Collectors.toList());
    }

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
        cmdr.runCommand(getCommands().get(label), args, Stream.concat(getExtraArguments(), Stream.of(this, sender)).toArray());
        return true;
    }
}
