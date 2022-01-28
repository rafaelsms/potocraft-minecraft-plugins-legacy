package com.rafaelsms.potocraft.database.pojo;

import java.time.ZonedDateTime;

public class HomeObject extends LocationObject {

    private String name;
    private ZonedDateTime creationDate;

    public HomeObject() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }
}
