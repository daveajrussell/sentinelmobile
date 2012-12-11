package com.sentinel.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * David Russell
 * 10/12/12
 */
public class SentinelSharedPreferences {

    private Context m_oContext;
    private SharedPreferences oSentinelSharedPreferences;
    private static final String SENTINEL_SHARED_PREFS;
    private static final String USER_IDENTIFICATION;

    static {
        SENTINEL_SHARED_PREFS = "SENTINEL_SHARED_PREFS";
        USER_IDENTIFICATION = "USER_IDENTIFICATION";
    }

    public SentinelSharedPreferences(Context context) {
        m_oContext = context;
        oSentinelSharedPreferences = m_oContext.getSharedPreferences(SENTINEL_SHARED_PREFS, Activity.MODE_PRIVATE);
    }

    public void setUserPreferences(String strUserIdentification) {
        SharedPreferences.Editor oSentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        oSentinelSharedPreferencesEditor.putString(USER_IDENTIFICATION, strUserIdentification);
        oSentinelSharedPreferencesEditor.apply();
    }

    public String getUserPreferences() {
        return oSentinelSharedPreferences.getString(USER_IDENTIFICATION, "");
    }

}
