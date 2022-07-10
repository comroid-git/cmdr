package org.comroid.cmdr.javacord;

import org.comroid.cmdr.CommandManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

public class JavacordCmdr extends CommandManager implements SlashCommandCreateListener {
    public JavacordCmdr(DiscordApi api) {
        api.getGlobalSlashCommands();
        api.getServers().forEach(api::getServerSlashCommands);
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
    }
}
