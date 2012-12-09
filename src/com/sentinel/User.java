package com.sentinel;

import java.util.UUID;

public class User {

    private UUID m_oUserIdentification;

    public User() {
    }

    public User(UUID oUserIdentification) {
        setM_oUserIdentification(oUserIdentification);
    }

    public UUID getM_oUserIdentification() {
        return m_oUserIdentification;
    }

    public void setM_oUserIdentification(UUID m_oUserIdentification) {
        this.m_oUserIdentification = m_oUserIdentification;
    }
}
