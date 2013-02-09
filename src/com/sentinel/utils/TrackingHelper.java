package com.sentinel.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import com.sentinel.models.GeospatialInformation;
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.tracking.SentinelLocationService;

import java.util.List;

/**
 * David Russell
 * 24/01/13
 */
public class TrackingHelper {

    public static Location lastKnownLocation(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);

        Location location = null;

        for (int i = providers.size() - 1; i >= 0; i--) {
            location = locationManager.getLastKnownLocation(providers.get(i));
            if (location != null)
                break;
        }

        return location;
    }

    public static GeospatialInformation getGeospatialInformation(final Context context) {
        SentinelSharedPreferences oSentinelSharedPreferences = new SentinelSharedPreferences(context);
        Location location = lastKnownLocation(context);

        return new GeospatialInformation
                (
                        oSentinelSharedPreferences.getSessionID(),
                        oSentinelSharedPreferences.getUserIdentification(),
                        System.currentTimeMillis(),
                        location.getLongitude(),
                        location.getLatitude(),
                        location.getSpeed(),
                        context.getResources().getConfiguration().orientation
                );
    }

    public static void startLocationService(final Context context) {
        Intent locationServicesIntent = new Intent(context, SentinelLocationService.class);
        context.startService(locationServicesIntent);
    }

    public static void stopLocationService(final Context context) {
        Intent locationServicesIntent = new Intent(context, SentinelLocationService.class);
        context.stopService(locationServicesIntent);
    }
}
