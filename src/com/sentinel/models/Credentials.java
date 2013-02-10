package com.sentinel.models;

/**
 * David Russell
 * 09/12/12
 */
public class Credentials {
    private String mUsername;
    private String mPassword;

    public Credentials(String username, String password) {
        mUsername = username;
        mPassword = password;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String username) {
        mUsername = username;
    }
}
