package com.sentinel;

/**
 * David Russell
 * 09/12/12
 */
public class Credentials {
    private String m_strUsername;
    private String m_strPassword;

    public Credentials() {

    }

    public Credentials(String strUsername, String strPassword) {
        this.m_strUsername = strUsername;
        this.m_strPassword = strPassword;
    }

    public String getPassword() {
        return m_strPassword;
    }

    public void setPassword(String m_strPassword) {
        this.m_strPassword = m_strPassword;
    }

    public String getUsername() {
        return m_strUsername;
    }

    public void setUsername(String m_strUsername) {
        this.m_strUsername = m_strUsername;
    }
}
