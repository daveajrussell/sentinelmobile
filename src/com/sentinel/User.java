package com.sentinel;

import java.util.UUID;

public class User {

    private UUID oUserIdentification;

    public User() {
    }

    public User(UUID oUserIdentification) {
        setUserIdentification(oUserIdentification);
    }

    public UUID getUserIdentification() {
        return oUserIdentification;
    }

    public void setUserIdentification(UUID oUserIdentification) {
        this.oUserIdentification = oUserIdentification;
    }
}
