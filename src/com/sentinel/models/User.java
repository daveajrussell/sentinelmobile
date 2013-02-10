package com.sentinel.models;

public class User {
    private String mUserKey;
    private int mSessionID;

    public User() {
    }

    public User(String userIdentification, int sessionID) {
        mUserKey = userIdentification;
        mSessionID = sessionID;
    }

    public String getUserIdentification() {
        return mUserKey;
    }

    public void setUserIdentification(String userIdentification) {
        mUserKey = userIdentification;
    }

    public int getSessionID() {
        return mSessionID;
    }

    public void setSessionID(int sessionID) {
        mSessionID = sessionID;
    }
}
