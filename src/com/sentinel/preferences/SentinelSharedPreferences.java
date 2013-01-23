package com.sentinel.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * David Russell
 * 10/12/12
 */
public class SentinelSharedPreferences {

    private SharedPreferences oSentinelSharedPreferences;
    private static final String SENTINEL_SHARED_PREFS;
    private static final String USER_IDENTIFICATION;
    private static final String SESSION_ID;

    static {
        SENTINEL_SHARED_PREFS = "SENTINEL_SHARED_PREFS";
        USER_IDENTIFICATION = "USER_IDENTIFICATION";
        SESSION_ID = "SESSION_ID";
    }

    public SentinelSharedPreferences(Context context) {
        oSentinelSharedPreferences = context.getSharedPreferences(SENTINEL_SHARED_PREFS, Activity.MODE_PRIVATE);
    }

    public void setUserPreferences(String strUserIdentification, int iSessionID) {
        SharedPreferences.Editor oSentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        oSentinelSharedPreferencesEditor.putString(USER_IDENTIFICATION, strUserIdentification);
        oSentinelSharedPreferencesEditor.putInt(SESSION_ID, iSessionID);
        oSentinelSharedPreferencesEditor.apply();
    }

    public String getUserIdentification() {
        return oSentinelSharedPreferences.getString(USER_IDENTIFICATION, "");
    }

    public int getSessionID() {
        return oSentinelSharedPreferences.getInt(SESSION_ID, 0);
    }

}
