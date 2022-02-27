package com.rafaelsms.potocraft.serverutility.commands;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookEmbed;
import com.rafaelsms.potocraft.serverutility.Permissions;
import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import com.rafaelsms.potocraft.util.TextUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class SendDiscordhookCommand implements CommandExecutor {

    private final @NotNull ServerUtilityPlugin plugin;

    public SendDiscordhookCommand(@NotNull ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!sender.hasPermission(Permissions.COMMAND_SEND_WEBHOOK)) {
            sender.sendMessage(plugin.getServer().getPermissionMessage());
            return true;
        }

        Optional<WebhookClient> clientOptional = plugin.getWebhookClient();
        if (clientOptional.isEmpty()) {
            return true;
        }
        Optional<String> joinStrings = TextUtil.joinStrings(args, 0);
        if (joinStrings.isEmpty()) {
            return true;
        }
        clientOptional.get()
                      .send(new WebhookEmbed(null,
                                             Color.ORANGE.getRGB(),
                                             joinStrings.get(),
                                             null,
                                             null,
                                             null,
                                             new WebhookEmbed.EmbedTitle("PotoCraft Utility", null),
                                             null,
                                             List.of()));
        return true;
    }
}
