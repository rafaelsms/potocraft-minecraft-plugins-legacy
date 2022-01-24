package com.rafaelsms.potocraft.serverutility.tasks;

import com.rafaelsms.potocraft.serverutility.ServerUtilityPlugin;
import org.bukkit.World;

import java.time.LocalTime;

public class SyncWorldTimeTask implements Runnable {

    private final ServerUtilityPlugin plugin;

    public SyncWorldTimeTask(ServerUtilityPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        LocalTime now = LocalTime.now();
        int time = now.getHour() * 1000 + (now.getMinute() * 1000 / 60) + (now.getSecond() * 1000 / 60 / 60);
        for (World world : plugin.getConfiguration().getSyncedTimeWorlds()) {
            long fullTime = world.getFullTime();
            fullTime -= (fullTime % 24_000);
            world.setFullTime(time - 6_000 + fullTime); // Shift time to Minecraft time (6:00 real -> 0:00 minecraft)
        }
    }
}
