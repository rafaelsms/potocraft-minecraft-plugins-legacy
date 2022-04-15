package com.rafaelsms.potocraft.combatserver.util;

import com.mongodb.client.model.Filters;
import com.rafaelsms.potocraft.database.CollectionProvider;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class InventoryContent implements CollectionProvider {

    private final @NotNull String kitName;

    private final Map<Integer, ItemStack> itemStackContent = new LinkedHashMap<>();
    private final Map<Integer, byte[]> serializedContent = new LinkedHashMap<>();

    public InventoryContent(@NotNull String kitName, ItemStack[] content) {
        this.kitName = kitName;
        for (int i = 0; i < content.length; i++) {
            if (content[i] != null) {
                itemStackContent.put(i, content[i]);
                serializedContent.put(i, content[i].serializeAsBytes());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public InventoryContent(@NotNull Document document) {
        kitName = document.getString(Keys.KIT_NAME);
        Map<String, Binary> map = document.get(Keys.INVENTORY_CONTENT, Map.class);
        for (Map.Entry<String, Binary> entry : map.entrySet()) {
            this.serializedContent.put(Integer.valueOf(entry.getKey()),
                                       Base64.getDecoder().decode(entry.getValue().getData()));
        }
        for (Map.Entry<Integer, byte[]> entry : serializedContent.entrySet()) {
            itemStackContent.put(entry.getKey(), ItemStack.deserializeBytes(entry.getValue()));
        }
    }

    public @NotNull String getKitName() {
        return kitName;
    }

    public void applyContent(@NotNull PlayerInventory playerInventory) {
        for (Map.Entry<Integer, ItemStack> entry : itemStackContent.entrySet()) {
            playerInventory.setItem(entry.getKey(), entry.getValue());
        }
    }

    public static Bson filterByName(@NotNull String kitName) {
        return Filters.eq(Keys.KIT_NAME, kitName);
    }

    public Bson filterByName() {
        return filterByName(kitName);
    }

    @Override
    public @NotNull Document toDocument() {
        Document document = new Document();
        document.put(Keys.KIT_NAME, kitName);
        Document serializedContent = new Document();
        for (Map.Entry<Integer, byte[]> entry : this.serializedContent.entrySet()) {
            serializedContent.put(String.valueOf(entry.getKey()),
                                  new Binary(Base64.getEncoder().encode(entry.getValue())));
        }
        document.put(Keys.INVENTORY_CONTENT, serializedContent);
        return document;
    }

    private static final class Keys {

        private static final String KIT_NAME = "kitName";
        private static final String INVENTORY_CONTENT = "inventoryContent";

        // Private constructor
        private Keys() {
        }
    }
}
