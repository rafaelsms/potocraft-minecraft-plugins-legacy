package com.rafaelsms.potocraft.database;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jetbrains.annotations.NotNull;

public interface CollectionProvider {

    @NotNull MongoCollection<Document> getCollection() throws DatabaseException;

    @NotNull Bson getIdQuery();

}
