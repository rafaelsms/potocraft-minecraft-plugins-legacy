package com.rafaelsms.potocraft.pet.listeners;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.pet.Permissions;
import com.rafaelsms.potocraft.pet.PetPlugin;
import com.rafaelsms.potocraft.pet.player.Profile;
import com.rafaelsms.potocraft.pet.player.User;
import net.kyori.adventure.text.Component;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

public class UserManager extends com.rafaelsms.potocraft.util.UserManager<User, Profile> {

    private final @NotNull PetPlugin plugin;

    public UserManager(@NotNull PetPlugin plugin) {
        super(plugin, plugin.getConfiguration().getSaveProfileTaskTimer());
        this.plugin = plugin;
    }

    @Override
    protected Component getKickMessageCouldNotLoadProfile() {
        return plugin.getConfiguration().getFailedToRetrieveProfile();
    }

    @Override
    protected Profile retrieveProfile(AsyncPlayerPreLoginEvent event) throws DatabaseException {
        return plugin.getDatabase()
                     .getProfile(event.getUniqueId())
                     .orElse(new Profile(event.getUniqueId(), event.getName()));
    }

    @Override
    protected User retrieveUser(PlayerLoginEvent event, @NotNull Profile profile) {
        return new User(plugin, event.getPlayer(), profile);
    }

    @Override
    protected void onLogin(User user) {
        // Update player name
        user.getProfile().setPlayerName(user.getPlayer().getName());
    }

    @Override
    protected void onJoin(User user) {
        // Spawn player's pet
        if (user.getProfile().isPetEnabled() && user.getPlayer().hasPermission(Permissions.PET_PERMISSION)) {
            user.spawnPet();
        }
    }

    @Override
    protected void onQuit(User user) {
        user.despawnPet();
    }

    @Override
    protected void tickUser(User user) {
        user.tickPet();
    }

    @Override
    protected void saveUser(User user) {
        plugin.getDatabase().saveProfile(user.getProfile());
    }
}
