package org.comroid.cmdr.spigot;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.comroid.cmdr.CommandManager;
import org.comroid.cmdr.model.Cmdr;
import org.comroid.cmdr.model.CommandBlob;
import org.comroid.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class SpigotCmdr extends JavaPlugin implements Cmdr.Underlying, MessageColorizer {
    public static final MessageColorizer HintColorizer = new MessageColorizer.Impl(ChatColor.GREEN, ChatColor.AQUA);
    public static final MessageColorizer InfoColorizer = new MessageColorizer.Impl(ChatColor.AQUA, ChatColor.GREEN);
    public static final MessageColorizer WarnColorizer = new MessageColorizer.Impl(ChatColor.YELLOW, ChatColor.RED);
    public static final MessageColorizer ErrorColorizer = new MessageColorizer.Impl(ChatColor.RED, ChatColor.GOLD);
    public static final MessageColorizer ExceptionColorizer = new MessageColorizer.Impl(ChatColor.DARK_RED, ChatColor.LIGHT_PURPLE);
    private final CommandManager cmdr = new CommandManager();
    private final MessageColorizer colorizer;

    @Override
    public ChatColor getPrimaryColor() {
        return colorizer.getPrimaryColor();
    }

    @Override
    public ChatColor getSecondaryColor() {
        return colorizer.getSecondaryColor();
    }

    @Override
    public ChatColor getDecorationColor() {
        return colorizer.getDecorationColor();
    }

    public SpigotCmdr() {
        this(InfoColorizer);
    }

    public SpigotCmdr(MessageColorizer colorizer) {
        this.colorizer = colorizer;
    }

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
        return cmdr.autoComplete(
                    getCommands().get(alias),
                    Stream.concat(Stream.of(alias), Stream.of(args)).toArray(String[]::new),
                    Stream.concat(getExtraArguments(), Stream.of(sender)).toArray()
                )
                .filter(txt -> txt.startsWith(args[args.length - 1]))
                .collect(Collectors.toList());
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
    public Object handleThrowable(Throwable t) {
        t.printStackTrace();
        return ExceptionColorizer.makeMessage("An unhandled %s occurred: %s", t.getClass().getSimpleName(), t.getMessage());
    }

    @Override
    public Object handleInvalidArguments(CommandBlob cmd, String[] gameArgs) {
        return ErrorColorizer.makeMessage("Invalid arguments: " + Arrays.toString(gameArgs) + " minimum: " + cmd
                .getParameters().stream().filter(x -> x.required).count());
    }

    @Override
    public void handleResponse(Object response, Object[] extraArgs) {
        CommandSender sender = ArrayUtil.get(extraArgs, CommandSender.class);
        sender.sendMessage(getChatPrefix() + ' ' + response);
    }

    public String getChatPrefix() {
        return getDecorationColor() + "["
                + getSecondaryColor() + getName()
                + getDecorationColor() + ']';
    }
}
