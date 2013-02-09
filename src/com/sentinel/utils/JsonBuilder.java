package com.sentinel.utils;

import android.content.Context;
import android.location.Location;
import com.google.gson.Gson;
import com.sentinel.models.Credentials;
import com.sentinel.models.GeospatialInformation;
import com.sentinel.preferences.SentinelSharedPreferences;
import org.json.JSONStringer;

public abstract class JsonBuilder {
    public static String userCredentialsJson(final Credentials credentials) {
        try {
            return new Gson().toJson(new JSONStringer()
                    .object()
                    .key("strUsername").value(credentials.getUsername())
                    .key("strPassword").value(credentials.getPassword())
                    .endObject().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String userCredentialsJson(Context context) {
        SentinelSharedPreferences sentinelSharedPreferences = new SentinelSharedPreferences(context);
        try {
            return new Gson().toJson(new JSONStringer()
                    .object()
                    .key("oUserIdentification").value(sentinelSharedPreferences.getUserIdentification())
                    .key("iSessionID").value(sentinelSharedPreferences.getSessionID())
                    .endObject().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String geospatialDataJson(final Context context, final Location location) {
        try {
            SentinelSharedPreferences sentinelSharedPreferences = new SentinelSharedPreferences(context);
            return new Gson().toJson(new JSONStringer()
                    .object()
                    .key("iSessionID").value(sentinelSharedPreferences.getSessionID())
                    .key("oUserIdentification").value(sentinelSharedPreferences.getUserIdentification())
                    .key("lTimeStamp").value(System.currentTimeMillis())
                    .key("dLatitude").value(location.getLatitude())
                    .key("dLongitude").value(location.getLongitude())
                    .key("dSpeed").value(location.getSpeed())
                    .key("iOrientation").value(context.getResources().getConfiguration().orientation)
                    .endObject().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String geoTaggedAssetJson(Context context, String assetID) {
        SentinelSharedPreferences sentinelSharedPreferences = new SentinelSharedPreferences(context);
        GeospatialInformation lastKnownLocation = TrackingHelper.getGeospatialInformation(context);

        try {
            return new Gson().toJson(new JSONStringer()
                    .object()
                    .key("oAssetKey").value(assetID)
                    .key("iSessionID").value(sentinelSharedPreferences.getSessionID())
                    .key("oUserIdentification").value(sentinelSharedPreferences.getUserIdentification())
                    .key("lTimeStamp").value(System.currentTimeMillis())
                    .key("dLatitude").value(lastKnownLocation.getLatitude())
                    .key("dLongitude").value(lastKnownLocation.getLongitude())
                    .key("dSpeed").value(lastKnownLocation.getSpeed())
                    .key("iOrientation").value(lastKnownLocation.getOrientation())
                    .endObject().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
