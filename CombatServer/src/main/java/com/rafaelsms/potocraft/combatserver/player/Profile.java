package com.rafaelsms.potocraft.combatserver.player;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.database.IdentifiedDatabaseObject;
import com.rafaelsms.potocraft.database.fields.CachedField;
import com.rafaelsms.potocraft.database.serializers.SimpleSerializer;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Profile extends IdentifiedDatabaseObject {

    private final @NotNull CombatServerPlugin plugin;

    private final CachedField<Integer> armorUpgrades;
    private final CachedField<Integer> weaponUpgrades;
    private final CachedField<Integer> consumablesUpgrades;

    protected Profile(@NotNull CombatServerPlugin plugin, @NotNull UUID playerId) throws DatabaseException {
        super(playerId);
        this.plugin = plugin;
        this.armorUpgrades = new CachedField<>("armorUpgrades", SimpleSerializer.getSerializer(), this, 0);
        this.weaponUpgrades = new CachedField<>("weaponUpgrades", SimpleSerializer.getSerializer(), this, 0);
        this.consumablesUpgrades = new CachedField<>("consumablesUpgrades", SimpleSerializer.getSerializer(), this, 0);
        fetchAllFields().ifPresent(document -> {
            armorUpgrades.readFrom(document);
            weaponUpgrades.readFrom(document);
            consumablesUpgrades.readFrom(document);
        });
    }

    public static Profile fetch(@NotNull CombatServerPlugin plugin, @NotNull UUID playerId) throws DatabaseException {
        return new Profile(plugin, playerId);
    }

    @Override
    public @NotNull MongoCollection<Document> getCollection() throws DatabaseException {
        return plugin.getDatabase().getProfileCollection();
    }

    @Override
    protected List<CachedField<?>> getRegisteredFields() {
        return List.of(armorUpgrades, weaponUpgrades, consumablesUpgrades);
    }
}
