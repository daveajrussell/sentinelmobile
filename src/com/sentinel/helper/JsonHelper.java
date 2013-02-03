package com.sentinel.helper;

import android.content.Context;
import com.google.gson.Gson;
import com.sentinel.models.Credentials;
import com.sentinel.preferences.SentinelSharedPreferences;
import org.json.JSONStringer;

/**
 * David Russell
 * 27/01/13
 */
public class JsonHelper {
    public static String getUserCredentialsJsonFromCredentials(final Credentials credentials) {
        try {
            return new Gson().toJson(new JSONStringer()
                    .object()
                    .key("strUsername").value(credentials.getUsername())
                    .key("strPassword").value(credentials.getPassword())
                    .endObject().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String getUserCredentialsJsonFromSharedPreferences(final SentinelSharedPreferences sharedPreferences) {
        try {
            return new Gson().toJson(new JSONStringer()
                    .object()
                    .key("iSessionID").value(sharedPreferences.getSessionID())
                    .key("oUserIdentification").value(sharedPreferences.getUserIdentification())
                    .endObject().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
