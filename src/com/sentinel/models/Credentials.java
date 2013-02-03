package com.sentinel.models;

/**
 * David Russell
 * 09/12/12
 */
public class Credentials {
    private String strUsername;
    private String strPassword;

    public Credentials(String strUsername, String strPassword) {
        this.strUsername = strUsername;
        this.strPassword = strPassword;
    }

    public String getPassword() {
        return strPassword;
    }

    public void setPassword(String strPassword) {
        this.strPassword = strPassword;
    }

    public String getUsername() {
        return strUsername;
    }

    public void setUsername(String strUsername) {
        this.strUsername = strUsername;
    }
}
