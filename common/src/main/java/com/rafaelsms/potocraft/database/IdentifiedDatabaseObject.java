package com.rafaelsms.potocraft.database;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.rafaelsms.potocraft.database.fields.CachedField;
import com.rafaelsms.potocraft.database.fields.IdentifierField;
import com.rafaelsms.potocraft.database.fields.UncachedField;
import com.rafaelsms.potocraft.database.serializers.Serializer;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class IdentifiedDatabaseObject<T> implements CollectionProvider {

    private final IdentifierField<T> idField;

    protected IdentifiedDatabaseObject(@NotNull Serializer<T> serializer, @NotNull T id) {
        this.idField = new IdentifierField<>(serializer, id);
    }

    @Override
    public @NotNull Bson getIdQuery() {
        return Filters.eq(idField.getKey(), getId());
    }

    public @NotNull T getId() {
        return idField.get().orElseThrow();
    }

    public void save() throws DatabaseException {
        Optional<Bson> updateDocument = getUpdateDocument();
        if (updateDocument.isEmpty()) {
            setFieldsUpdated();
            return;
        }
        getCollection().updateOne(getIdQuery(), updateDocument.get());
        setFieldsUpdated();
    }

    private void setFieldsUpdated() {
        getRegisteredFields().forEach(CachedField::setUpdated);
    }

    private Optional<Bson> getUpdateDocument() {
        List<Bson> updates = new ArrayList<>();
        for (CachedField<?> field : getRegisteredFields()) {
            field.getUpdateDocument().ifPresent(updates::add);
        }
        if (updates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Updates.combine(updates));
    }

    private @NotNull Bson getAllFieldsProjection() {
        List<Bson> fieldProjections = new ArrayList<>();
        for (UncachedField<?> field : getRegisteredFields()) {
            fieldProjections.add(field.getFieldProjection());
        }
        return Projections.fields(fieldProjections);
    }

    private @NotNull Optional<Document> fetchDocument(@NotNull Bson projection) throws DatabaseException {
        return Optional.ofNullable(getCollection().find(getIdQuery()).projection(projection).first());
    }

    private @NotNull Optional<Document> fetchAllFields() throws DatabaseException {
        return fetchDocument(getAllFieldsProjection());
    }

    protected void fetchDatabaseObjectIfPresent() throws DatabaseException {
        fetchAllFields().ifPresent(document -> {
            for (CachedField<?> registeredField : getRegisteredFields()) {
                registeredField.readFrom(document);
            }
        });
    }

    protected abstract List<CachedField<?>> getRegisteredFields();
}
