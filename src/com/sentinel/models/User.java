package com.sentinel.models;

public class User
{
    private String UserKey;
    private int SessionID;

    public User()
    {
    }

    public User(String oUserIdentification, int iSessionID)
    {
        this.UserKey = oUserIdentification;
        this.SessionID = iSessionID;
    }

    public String getUserIdentification()
    {
        return UserKey;
    }

    public void setUserIdentification(String oUserIdentification)
    {
        this.UserKey = oUserIdentification;
    }

    public int getSessionID()
    {
        return SessionID;
    }

    public void setSessionID(int iSessionID)
    {
        this.SessionID = iSessionID;
    }
}
