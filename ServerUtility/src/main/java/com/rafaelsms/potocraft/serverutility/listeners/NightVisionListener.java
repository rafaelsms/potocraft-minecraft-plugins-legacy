package com.rafaelsms.potocraft.serverutility.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class NightVisionListener implements Listener {

    private void removeNightVision(@NotNull Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }

    private void giveNightVision(@NotNull Player player) {
        removeNightVision(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1));
    }

    @EventHandler
    private void giveNightVision(PlayerGameModeChangeEvent event) {
        if (event.getNewGameMode() == GameMode.SPECTATOR) {
            giveNightVision(event.getPlayer());
        } else if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            removeNightVision(event.getPlayer());
        }
    }

    @EventHandler
    private void giveNightVision(PlayerJoinEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SPECTATOR) {
            giveNightVision(event.getPlayer());
        }
    }
}
