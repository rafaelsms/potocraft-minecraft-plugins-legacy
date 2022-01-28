package com.rafaelsms.potocraft.database.pojo;

import org.bson.types.ObjectId;

public abstract class BaseObject {

    private ObjectId id;

    public BaseObject() {
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "BaseObject{" + "id=" + id + '}';
    }
}
