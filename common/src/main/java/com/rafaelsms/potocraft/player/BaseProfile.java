package com.rafaelsms.potocraft.player;

import com.rafaelsms.potocraft.database.IdentifiedDatabaseObject;
import com.rafaelsms.potocraft.database.serializers.UUIDSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class BaseProfile extends IdentifiedDatabaseObject<UUID> {

    protected BaseProfile(@NotNull UUID id) {
        super(UUIDSerializer.getSerializer(), id);
    }

    public @NotNull UUID getUniqueId() {
        return getId();
    }

}
