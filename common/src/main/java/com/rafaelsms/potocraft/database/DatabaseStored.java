package com.rafaelsms.potocraft.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class DatabaseStored {

    protected abstract List<CachedField<?>> getRegisteredFields();

    private void setFieldsUpdated() {
        getRegisteredFields().forEach(CachedField::setUpdated);
    }

    public void save() throws DatabaseException {
        Optional<Bson> updateDocument = getUpdateDocument();
        if (updateDocument.isEmpty()) {
            setFieldsUpdated();
            return;
        }
        getMongoCollection().updateOne(findQuery(), updateDocument.get());
        setFieldsUpdated();
    }

    public Optional<Bson> getUpdateDocument() {
        List<Bson> updates = new ArrayList<>();
        for (CachedField<?> field : getRegisteredFields()) {
            field.getUpdateDocument().ifPresent(updates::add);
        }
        if (updates.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(Updates.combine(updates));
    }

    protected abstract MongoCollection<Document> getMongoCollection() throws DatabaseException;

    protected abstract Bson findQuery();

}
