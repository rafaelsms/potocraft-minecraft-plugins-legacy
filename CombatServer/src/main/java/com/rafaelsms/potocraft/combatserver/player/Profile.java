package com.rafaelsms.potocraft.combatserver.player;

import com.mongodb.client.MongoCollection;
import com.rafaelsms.potocraft.combatserver.CombatServerPlugin;
import com.rafaelsms.potocraft.database.CachedField;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.database.IdentifiedDatabaseStored;
import com.rafaelsms.potocraft.database.SimpleField;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class Profile extends IdentifiedDatabaseStored {

    private final @NotNull CombatServerPlugin plugin;

    private final SimpleField<Integer> armorUpgrades;
    private final SimpleField<Integer> weaponUpgrades;
    private final SimpleField<Integer> consumablesUpgrades;

    protected Profile(@NotNull CombatServerPlugin plugin, @NotNull UUID playerId) throws DatabaseException {
        super(playerId);
        this.plugin = plugin;
        this.armorUpgrades = new SimpleField<>(TODO, "armorUpgrades", getCollectionProvider(), 0);
        this.weaponUpgrades = new SimpleField<>(TODO, "weaponUpgrades", getCollectionProvider(), 0);
        this.consumablesUpgrades = new SimpleField<>(TODO, "consumablesUpgrades", getCollectionProvider(), 0);
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
    protected MongoCollection<Document> getMongoCollection() throws DatabaseException {
        return plugin.getDatabase().getProfileCollection();
    }

    @Override
    protected List<CachedField<?>> getOtherRegisteredFields() {
        return List.of(armorUpgrades, weaponUpgrades, consumablesUpgrades);
    }
}
