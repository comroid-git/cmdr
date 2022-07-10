package org.comroid.cmdr.spigot;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.comroid.cmdr.CommandManager;
import org.comroid.cmdr.model.Cmdr;
import org.comroid.cmdr.model.Command;
import org.comroid.cmdr.model.CommandBlob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpigotCmdr extends JavaPlugin implements Cmdr {
    private final CommandManager cmds = new CommandManager();

    @Override
    public final Set<CommandBlob> register(Class<?> cls) {
        return cmds.register(cls);
    }

    @Override
    public final Map<String, CommandBlob> getCommands() {
        return cmds.getCommands();
    }

    @Nullable
    @Override
    public final List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String alias, @NotNull String[] args) {
    }

    @Override
    public final boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command command, @NotNull String label, @NotNull String[] args) {
    }
}
