package com.rafaelsms.potocraft.blockprotection.protection;

import com.rafaelsms.potocraft.database.DatabaseObject;
import com.rafaelsms.potocraft.util.Util;
import org.bson.Document;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Region extends DatabaseObject {

    private final @NotNull Map<UUID, Member> members = new HashMap<>();
    private final @NotNull ArrayList<BoundingBox> boxes = new ArrayList<>();

    private final @NotNull UUID regionId;
    private @NotNull String regionName;

    public Region(@NotNull Document document) {
        this.regionId = Util.convertNonNull(document.getString(Keys.REGION_ID), UUID::fromString);
        this.regionName = document.getString(Keys.REGION_NAME);
        this.members.putAll(Member.fromDocument(document.getList(Keys.MEMBERS, Document.class)));
    }

    public @NotNull UUID getRegionId() {
        return regionId;
    }

    public @NotNull String getRegionName() {
        return regionName;
    }

    public void setRegionName(@NotNull String regionName) {
        this.regionName = regionName;
    }

    public List<BoundingBox> getBoxes() {
        return Collections.unmodifiableList(boxes);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.REGION_ID, Util.convertNonNull(regionId, UUID::toString));
        document.put(Keys.REGION_NAME, regionName);
        document.put(Keys.MEMBERS, Member.toDocument(members.values()));
        return document;
    }

    private static final class Keys {

        private static final String REGION_ID = "_id";
        private static final String REGION_NAME = "regionName";

        private static final String MEMBERS = "members";
        private static final String BOXES = "boxes";

        // Private constructor
        private Keys() {
        }
    }

    private static class Member extends DatabaseObject {

        private final @NotNull UUID playerId;
        private final boolean owner;

        public Member(@NotNull UUID playerId, boolean owner) {
            this.playerId = playerId;
            this.owner = owner;
        }

        public Member(@NotNull Document document) {
            this.playerId = Util.convertNonNull(document.getString(Keys.PLAYER_ID), UUID::fromString);
            this.owner = document.getBoolean(Keys.IS_OWNER);
        }

        public @NotNull UUID getPlayerId() {
            return playerId;
        }

        public boolean isOwner() {
            return owner;
        }

        @Override
        public @NotNull Document toDocument() {
            Document document = new Document();
            document.put(Keys.PLAYER_ID, Util.convertNonNull(playerId, UUID::toString));
            document.put(Keys.IS_OWNER, owner);
            return document;
        }

        public static Map<UUID, Member> fromDocument(@NotNull List<Document> documents) {
            Map<UUID, Member> members = new HashMap<>();
            for (Document document : documents) {
                Member member = new Member(document);
                members.put(member.getPlayerId(), member);
            }
            return members;
        }

        public static List<Document> toDocument(@NotNull Collection<Member> members) {
            List<Document> documents = new ArrayList<>();
            for (Member member : members) {
                documents.add(member.toDocument());
            }
            return documents;
        }

        private static final class Keys {

            private static final String PLAYER_ID = "_id";
            private static final String IS_OWNER = "owner";

            // Private constructor
            private Keys() {
            }
        }
    }
}
