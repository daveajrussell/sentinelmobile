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
    private Gson gson;
    private SentinelSharedPreferences sentinelSharedPreferences;

    public JsonHelper(Context context) {
        gson = new Gson();
        sentinelSharedPreferences = new SentinelSharedPreferences(context);
    }

    public String getUserCredentialsJsonFromCredentials(Credentials credentials) {
        try {
            return gson.toJson(new JSONStringer()
                    .object()
                    .key("strUsername").value(credentials.getUsername())
                    .key("strPassword").value(credentials.getPassword())
                    .endObject().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public String getUserCredentialsJsonFromSharedPreferences() {
        try {
            return gson.toJson(new JSONStringer()
                    .object()
                    .key("iSessionID").value(sentinelSharedPreferences.getSessionID())
                    .key("oUserIdentification").value(sentinelSharedPreferences.getUserIdentification())
                    .endObject().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }
}
