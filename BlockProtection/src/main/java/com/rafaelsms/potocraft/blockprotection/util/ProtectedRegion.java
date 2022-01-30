package com.rafaelsms.potocraft.blockprotection.util;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ProtectedRegion extends DatabaseObject {

    private final HashMap<UUID, Boolean> members = new HashMap<>();
    private final @NotNull UUID regionId;

    private @NotNull Box regionBox;

    @SuppressWarnings("unchecked")
    public ProtectedRegion(@NotNull Document document) {
        this.regionId = Util.convertNonNull(document.getString(Keys.REGION_ID), UUID::fromString);
        this.members.putAll(convertDocumentToMembers((Map<String, Boolean>) Objects.requireNonNull(document)
                                                                                   .get(Keys.MEMBERS_KEY)));
        this.regionBox = new Box(document.get(Keys.BOX_KEY, Document.class));
    }

    public ProtectedRegion(@NotNull UUID owner, @NotNull Box startingBox) {
        this.regionId = UUID.randomUUID();
        this.members.put(owner, true);
        this.regionBox = startingBox;
    }

    public @NotNull UUID getRegionId() {
        return regionId;
    }

    public boolean addMember(@NotNull UUID playerId) {
        return this.members.put(playerId, false) == null;
    }

    public boolean setOwner(@NotNull UUID playerId) {
        if (!members.containsKey(playerId)) {
            return false;
        }
        this.members.put(playerId, true);
        return true;
    }

    public boolean isMember(@NotNull UUID playerId) {
        return members.containsKey(playerId);
    }

    public boolean isOwner(@NotNull UUID playerId) {
        return Boolean.TRUE.equals(members.get(playerId));
    }

    public @NotNull Box getRegionBoxes() {
        return regionBox;
    }

    public void setRegionBox(@NotNull Box regionBox) {
        this.regionBox = regionBox;
    }

    public static Bson filterColliding(@NotNull Bson boxFilter) {
        return Filters.eq(Keys.BOX_KEY, boxFilter);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.REGION_ID, Util.convertNonNull(regionId, UUID::toString));
        document.put(Keys.MEMBERS_KEY, convertMembersToDocument(members));
        document.put(Keys.BOX_KEY, regionBox.toDocument());
        return document;
    }

    private static Map<UUID, Boolean> convertDocumentToMembers(Map<String, Boolean> members) {
        HashMap<UUID, Boolean> map = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : members.entrySet()) {
            map.put(Util.convertNonNull(entry.getKey(), UUID::fromString), entry.getValue());
        }
        return map;
    }

    private static Map<String, Boolean> convertMembersToDocument(Map<UUID, Boolean> members) {
        HashMap<String, Boolean> map = new HashMap<>();
        for (Map.Entry<UUID, Boolean> entry : members.entrySet()) {
            map.put(Util.convertNonNull(entry.getKey(), UUID::toString), entry.getValue());
        }
        return map;
    }

    private final static class Keys {

        public static final String REGION_ID = "_id";
        public static final String MEMBERS_KEY = "members";
        public static final String BOX_KEY = "box";

        // Private constructor
        private Keys() {
        }
    }
}
