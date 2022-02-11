package com.rafaelsms.discordbot.listeners;

import com.rafaelsms.discordbot.DiscordBot;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class SetActivityListener extends ListenerAdapter {

    public SetActivityListener(@NotNull DiscordBot bot) {
        bot.getJda()
           .getPresence()
           .setPresence(OnlineStatus.DO_NOT_DISTURB,
                        Activity.playing(bot.getConfiguration().getBotPlayingActivity()),
                        false);
    }
}
