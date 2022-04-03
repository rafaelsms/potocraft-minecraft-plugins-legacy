package com.rafaelsms.teleporter.player;

import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.player.BaseUser;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class User extends BaseUser<Profile> {

    public User(@Nullable Profile profile, @Nullable Player player) throws DatabaseException {
        super(profile, player);
    }
}
