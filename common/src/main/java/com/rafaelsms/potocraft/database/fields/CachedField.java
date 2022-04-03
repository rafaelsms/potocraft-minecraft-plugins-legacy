package com.rafaelsms.potocraft.database.fields;

import com.mongodb.client.model.Updates;
import com.rafaelsms.potocraft.database.CollectionProvider;
import com.rafaelsms.potocraft.database.DatabaseException;
import com.rafaelsms.potocraft.database.serializers.Serializer;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class CachedField<T> extends UncachedField<T> {

    // Cached value
    private @Nullable T value;
    private boolean fetched = false;
    private boolean dirty = false;

    public CachedField(@NotNull String key,
                       @NotNull Serializer<T> serializer,
                       @NotNull CollectionProvider provider,
                       @Nullable T defaultValue) {
        super(key, serializer, provider);
        this.value = defaultValue;
    }

    public CachedField(@NotNull String key, @NotNull Serializer<T> serializer, @NotNull CollectionProvider provider) {
        this(key, serializer, provider, null);
    }

    @Override
    public @NotNull Optional<T> get() throws DatabaseException {
        if (!fetched) {
            Optional<T> optional = super.get();
            // Keep value (default value or null) if database has null
            this.value = optional.orElse(value);
            this.fetched = true;
            this.dirty = false;
            return optional;
        }
        return Optional.ofNullable(value);
    }

    /**
     * Gets the value cached or throws.
     *
     * @return cached value (can be null) or throws.
     * @throws IllegalStateException if field was not fetched before.
     * @see CachedField#getOrDefault() for setting the default value (which can be null still) instead of throwing.
     */
    public @NotNull Optional<T> getIfFetched() {
        if (!fetched) {
            String message =
                    "Field value for \"%s\" wasn't either fetched or set before being queried!".formatted(getKey());
            throw new IllegalStateException(message);
        }
        return Optional.ofNullable(value);
    }

    /**
     * Gets the value cached if it was already fetched or sets the default value.
     * <p>
     * This WILL OVERWRITE DATA if the field was not previously fetched.
     *
     * @return cached value or default value. Both could be null.
     */
    public @NotNull Optional<T> getOrDefault() {
        if (!fetched) {
            // Use default value setting it as fetched
            set(value);
        }
        return Optional.ofNullable(value);
    }

    @Override
    public void set(@Nullable T value) {
        this.value = value;
        // Don't matter if it was fetched before
        this.fetched = true;
        this.dirty = true;
    }

    /**
     * Sets the value only if it was previously fetched.
     *
     * @param value value to be set.
     * @throws IllegalStateException if value wasn't fetched before.
     */
    public void setIfFetched(@Nullable T value) {
        if (!fetched) {
            throw new IllegalStateException("Couldn't set value as field wasn't previously fetched.");
        }
        set(value);
    }

    public void readFrom(@Nullable Document document) {
        if (document != null) {
            // If nothing on document, keep with value (initialized with default or null value)
            this.value = getSerializer().fromDocument(document.get(getKey(), value));
        }
        this.fetched = true;
        this.dirty = false;
    }

    public @NotNull Optional<Bson> getUpdateDocument() {
        if (!fetched || !dirty) {
            return Optional.empty();
        }
        return Optional.of(Updates.set(getKey(), getSerializer().toDocument(value)));
    }

    public void setUpdated() {
        this.fetched = true;
        this.dirty = false;
    }
}
