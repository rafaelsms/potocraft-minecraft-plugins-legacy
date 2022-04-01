package com.rafaelsms.potocraft.player;

import com.rafaelsms.potocraft.database.IdentifiedDatabaseStored;
import com.rafaelsms.potocraft.database.UncachedField;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public abstract class BaseProfile extends IdentifiedDatabaseStored {

    // TODO non-cached fields (always fetched) for nickname

    protected BaseProfile(@NotNull UUID id) {
        super(id);
    }

    @Override
    protected List<UncachedField<?>> getOtherRegisteredFields() {
        return null;
    }
}
