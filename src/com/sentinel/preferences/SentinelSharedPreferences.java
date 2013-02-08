package com.sentinel.preferences;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * David Russell
 * 10/12/12
 */
public class SentinelSharedPreferences {

    private static final String SENTINEL_SHARED_PREFS;
    private static final String USER_IDENTIFICATION;
    private static final String SESSION_ID;
    private static final String SESSION_BEGIN_DATE_TIME;
    private static final String BREAK_TAKEN_DATE_TIME;
    private static final String DRIVING_END_ALARM;
    private static final String CLOCKED_IN;
    private static final String CLOCKED_OUT;
    private static final String BREAK_LENGTH;
    //private static final String NEXT_BREAK_LENGTH;
    //private static final String NEXT_BREAK;

    static {
        SENTINEL_SHARED_PREFS = "SENTINEL_SHARED_PREFS";
        USER_IDENTIFICATION = "USER_IDENTIFICATION";
        SESSION_ID = "SESSION_ID";
        SESSION_BEGIN_DATE_TIME = "SESSION_BEGIN_DATE_TIME";
        BREAK_TAKEN_DATE_TIME = "BREAK_TAKEN_DATE_TIME";
        DRIVING_END_ALARM = "DRIVING_END_ALARM";
        CLOCKED_IN = "CLOCKED_IN";
        CLOCKED_OUT = "CLOCKED_OUT";
        BREAK_LENGTH = "BREAK_LENGTH";
        //NEXT_BREAK_LENGTH = "NEXT_BREAK_LENGTH";
        //NEXT_BREAK = "NEXT_BREAK";
    }

    private SharedPreferences oSentinelSharedPreferences;

    public SentinelSharedPreferences(Context context) {
        oSentinelSharedPreferences = context.getSharedPreferences(SENTINEL_SHARED_PREFS, Activity.MODE_PRIVATE);
    }

    public void clearSharedPreferences() {
        SharedPreferences.Editor sentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        sentinelSharedPreferencesEditor.clear();
        sentinelSharedPreferencesEditor.apply();
    }

    public void setUserPreferences(String strUserIdentification, int iSessionID) {
        SharedPreferences.Editor oSentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        oSentinelSharedPreferencesEditor.putString(USER_IDENTIFICATION, strUserIdentification);
        oSentinelSharedPreferencesEditor.putInt(SESSION_ID, iSessionID);
        oSentinelSharedPreferencesEditor.apply();
    }

    public void setSessionBeginDateTime(long lngSessionBeginTicks) {
        SharedPreferences.Editor oSentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        oSentinelSharedPreferencesEditor.putLong(SESSION_BEGIN_DATE_TIME, lngSessionBeginTicks);
        oSentinelSharedPreferencesEditor.apply();
    }

    public void setBreakTakenDateTime(long lngBreakTakenTicks) {
        SharedPreferences.Editor oSentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        oSentinelSharedPreferencesEditor.putLong(BREAK_TAKEN_DATE_TIME, lngBreakTakenTicks);
        oSentinelSharedPreferencesEditor.apply();
    }

    public void setDrivingEndAlarm(long lngDrivingEndAlarm) {
        SharedPreferences.Editor oSentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        oSentinelSharedPreferencesEditor.putLong(DRIVING_END_ALARM, lngDrivingEndAlarm);
        oSentinelSharedPreferencesEditor.apply();
    }

    public void setClockedIn() {
        SharedPreferences.Editor sentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        sentinelSharedPreferencesEditor.putBoolean(CLOCKED_IN, true);
        sentinelSharedPreferencesEditor.putBoolean(CLOCKED_OUT, false);
        sentinelSharedPreferencesEditor.apply();
    }

    public void setClockedOut() {
        SharedPreferences.Editor sentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        sentinelSharedPreferencesEditor.putBoolean(CLOCKED_IN, false);
        sentinelSharedPreferencesEditor.putBoolean(CLOCKED_OUT, true);
        sentinelSharedPreferencesEditor.apply();
    }

    public void setBreakLength(long lngBreakLength) {
        SharedPreferences.Editor sentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        sentinelSharedPreferencesEditor.putLong(BREAK_LENGTH, lngBreakLength);
        sentinelSharedPreferencesEditor.apply();
    }

    /*public void setNextBreak(long lngNextBreak) {
        SharedPreferences.Editor sentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        sentinelSharedPreferencesEditor.putLong(NEXT_BREAK, lngNextBreak);
        sentinelSharedPreferencesEditor.apply();
    }

    public void setNextBreakLength(long lngNextBreakLength) {
        SharedPreferences.Editor sentinelSharedPreferencesEditor = oSentinelSharedPreferences.edit();
        sentinelSharedPreferencesEditor.putLong(NEXT_BREAK_LENGTH, lngNextBreakLength);
        sentinelSharedPreferencesEditor.apply();
    }*/

    public boolean clockedIn() {
        return oSentinelSharedPreferences.getBoolean(CLOCKED_IN, false);
    }

    public boolean clockedOut() {
        return oSentinelSharedPreferences.getBoolean(CLOCKED_OUT, false);
    }

    public String getUserIdentification() {
        return oSentinelSharedPreferences.getString(USER_IDENTIFICATION, "");
    }

    public int getSessionID() {
        return oSentinelSharedPreferences.getInt(SESSION_ID, 0);
    }

    public long getSessionBeginDateTime() {
        return oSentinelSharedPreferences.getLong(SESSION_BEGIN_DATE_TIME, 0);
    }

    public long getDrivingEndAlarm() {
        return oSentinelSharedPreferences.getLong(DRIVING_END_ALARM, 0);
    }

    public long getBreakStartDateTime() {
        return oSentinelSharedPreferences.getLong(BREAK_TAKEN_DATE_TIME, 0);
    }

    public long getBreakLength() {
        return oSentinelSharedPreferences.getLong(BREAK_LENGTH, 0);
    }

    public long getBreakTakenTime() {
        return oSentinelSharedPreferences.getLong(BREAK_TAKEN_DATE_TIME, 0);
    }

    /*public long getNextBreak() {
        return oSentinelSharedPreferences.getLong(NEXT_BREAK, 0);
    }

    public long getNextBreakLength() {
        return oSentinelSharedPreferences.getLong(NEXT_BREAK_LENGTH, 0);
    }*/

}
