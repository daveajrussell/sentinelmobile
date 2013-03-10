package com.sentinel.utils;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import com.sentinel.models.GeospatialInformation;
import com.sentinel.preferences.SentinelSharedPreferences;
import com.sentinel.services.SentinelLocationService;

import java.util.List;

public abstract class TrackingHelper {

    public static Location lastKnownLocation(final Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);

        Location gpsProvidedLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location otherProviderLocation = null;

        if (null != gpsProvidedLocation) {
            return gpsProvidedLocation;
        } else {
            for (int i = providers.size() - 1; i >= 0; i--) {

                otherProviderLocation = locationManager.getLastKnownLocation(providers.get(i));

                if (otherProviderLocation != null)
                    break;
            }
        }

        return otherProviderLocation;
    }

    public static GeospatialInformation getGeospatialInformation(final Context context, final Location location) {
        SentinelSharedPreferences oSentinelSharedPreferences = new SentinelSharedPreferences(context);
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

    public static Criteria getGeoSpatialCriteria() {
        new CriteriaBuilder()
                .setAccuracy(Criteria.ACCURACY_FINE)
                .setPowerRequirement(Criteria.POWER_HIGH)
                .setAltitudeRequired(false)
                .setBearingRequired(false)
                .setSpeedRequired(true)
                .setCostAllowed(true);
        return CriteriaBuilder.build();
    }
}
