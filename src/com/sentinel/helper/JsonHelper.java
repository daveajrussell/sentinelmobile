package com.sentinel.helper;

import android.content.Context;
import android.location.Location;
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

    public static String getGeospatialJsonString(final Context context, final Location location) {
        try {
            SentinelSharedPreferences sentinelSharedPreferences = new SentinelSharedPreferences(context);
            JSONStringer strBufferedData = new JSONStringer()
                    .object()
                    .key("iSessionID").value(sentinelSharedPreferences.getSessionID())
                    .key("oUserIdentification").value(sentinelSharedPreferences.getUserIdentification())
                    .key("lTimeStamp").value(System.currentTimeMillis())
                    .key("dLatitude").value(location.getLatitude())
                    .key("dLongitude").value(location.getLongitude())
                    .key("dSpeed").value(location.getSpeed())
                    .key("iOrientation").value(context.getResources().getConfiguration().orientation)
                    .endObject();

            return new Gson().toJson(strBufferedData.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
