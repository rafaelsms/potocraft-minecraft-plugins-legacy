package com.rafaelsms.potocraft.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public abstract class IdentifiedDatabaseStored extends DatabaseStored {

    public static final String ID_KEY = "_id";

    protected final CollectionProvider collectionProvider = new CollectionProvider();
    protected final UUIDField idField;

    protected IdentifiedDatabaseStored(@NotNull UUID id) {
        this.idField = new UUIDField(null, ID_KEY, id);
    }

    protected abstract List<CachedField<?>> getOtherRegisteredFields();

    protected @NotNull Bson getAllFieldsProjection() {
        List<Bson> fieldProjections = new ArrayList<>();
        for (UncachedField<?> field : getRegisteredFields()) {
            fieldProjections.add(field.getFieldProjection());
        }
        return Projections.fields(fieldProjections);
    }

    @Override
    protected List<CachedField<?>> getRegisteredFields() {
        return getOtherRegisteredFields();
    }

    @Override
    protected Bson findQuery() {
        return Filters.eq(ID_KEY, idField.get().orElseThrow());
    }

    protected com.rafaelsms.potocraft.database.CollectionProvider getCollectionProvider() {
        return collectionProvider;
    }

    protected @NotNull Optional<Document> fetchDocument(@NotNull Bson projection) throws DatabaseException {
        return Optional.ofNullable(getMongoCollection().find(findQuery()).projection(projection).first());
    }

    protected @NotNull Optional<Document> fetchAllFields() throws DatabaseException {
        return fetchDocument(getAllFieldsProjection());
    }

    private class CollectionProvider implements com.rafaelsms.potocraft.database.CollectionProvider {

        @Override
        public @NotNull MongoCollection<Document> getCollection() throws DatabaseException {
            return getMongoCollection();
        }

        @Override
        public @NotNull Bson getIdQuery() {
            return findQuery();
        }
    }
}
