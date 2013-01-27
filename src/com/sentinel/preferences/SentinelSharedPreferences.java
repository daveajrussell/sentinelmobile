package com.sentinel.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * David Russell
 * 10/12/12
 */
public class SentinelSharedPreferences
{

    private static final String SENTINEL_SHARED_PREFS;
    private static final String USER_IDENTIFICATION;
    private static final String SESSION_ID;
    private static final String SESSION_BEGIN_DATE_TIME;
    private static final String BREAK_TAKEN_DATE_TIME;
    private static final String NEXT_ALARM;
    private static final String DRIVING_END_ALARM;

    static
    {
        SENTINEL_SHARED_PREFS = "SENTINEL_SHARED_PREFS";
        USER_IDENTIFICATION = "USER_IDENTIFICATION";
        SESSION_ID = "SESSION_ID";
        SESSION_BEGIN_DATE_TIME = "SESSION_BEGIN_DATE_TIME";
        BREAK_TAKEN_DATE_TIME = "BREAK_TAKEN_DATE_TIME";
        NEXT_ALARM = "NEXT_ALARM";
        DRIVING_END_ALARM = "DRIVING_END_ALARM";
    }

    private SharedPreferences oSentinelSharedPreferences;

    public SentinelSharedPreferences(Context context)
    {
        oSentinelSharedPreferences = context.getSharedPreferences(SENTINEL_SHARED_PREFS, Activity.MODE_PRIVATE);
    }

    public void setUserPreferences(String strUserIdentification, int iSessionID)
    {
        SharedPreferences.Editor oSentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        oSentinelSharedPreferencesEditor.putString(USER_IDENTIFICATION, strUserIdentification);
        oSentinelSharedPreferencesEditor.putInt(SESSION_ID, iSessionID);
        oSentinelSharedPreferencesEditor.apply();
    }

    public void setSessionBeginDateTime(long lngSessionBeginTicks)
    {
        SharedPreferences.Editor oSentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        oSentinelSharedPreferencesEditor.putLong(SESSION_BEGIN_DATE_TIME, lngSessionBeginTicks);
        oSentinelSharedPreferencesEditor.apply();
    }

    public void setBreakTakenDateTime(long lngBreakTakenTicks)
    {
        SharedPreferences.Editor oSentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        oSentinelSharedPreferencesEditor.putLong(BREAK_TAKEN_DATE_TIME, lngBreakTakenTicks);
        oSentinelSharedPreferencesEditor.apply();
    }

    public void setNextAlarm(long lngNextAlarm)
    {
        SharedPreferences.Editor oSentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        oSentinelSharedPreferencesEditor.putLong(NEXT_ALARM, lngNextAlarm);
        oSentinelSharedPreferencesEditor.apply();
    }

    public void setDrivingEndAlarm(long lngDrivingEndAlarm)
    {
        SharedPreferences.Editor oSentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        oSentinelSharedPreferencesEditor.putLong(DRIVING_END_ALARM, lngDrivingEndAlarm);
        oSentinelSharedPreferencesEditor.apply();
    }

    public String getUserIdentification()
    {
        return oSentinelSharedPreferences.getString(USER_IDENTIFICATION, "");
    }

    public int getSessionID()
    {
        return oSentinelSharedPreferences.getInt(SESSION_ID, 0);
    }

    public long getSessionBeginDateTime()
    {
        return oSentinelSharedPreferences.getLong(SESSION_BEGIN_DATE_TIME, 0);
    }

    public long getBreakTakeDateTime()
    {
        return oSentinelSharedPreferences.getLong(BREAK_TAKEN_DATE_TIME, 0);
    }

    public long getNextAlarm()
    {
        return oSentinelSharedPreferences.getLong(NEXT_ALARM, 0);
    }

    public long getDrivingEndAlarm()
    {
        return oSentinelSharedPreferences.getLong(DRIVING_END_ALARM, 0);
    }

}
