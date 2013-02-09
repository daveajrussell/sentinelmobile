package com.sentinel.utils;

import android.location.Location;

import java.util.concurrent.TimeUnit;

public abstract class Utils {
    public static String getFormattedHrsMinsSecsTimeString(final long timeInMillis) {
        return String.format("%d:%d:%d",
                TimeUnit.MILLISECONDS.toHours(timeInMillis),
                TimeUnit.MILLISECONDS.toMinutes(timeInMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toHours(timeInMillis)),
                TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillis)));

    }

    public static String getFormattedMinsSecsTimeString(final long timeInMillis) {

        return String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(timeInMillis),
                TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillis)));
    }

    public static boolean checkUpdateIsMoreAccurate(Location lastLocation, Location newLocation, final int TIME_BETWEEN_UPDATES) {
        if (lastLocation == null) {
            return true;
        }

        long locationTimeDifference = newLocation.getTime() - lastLocation.getTime();
        boolean newLocationIsMoreRecent = locationTimeDifference > 0;

        if (locationTimeDifference > TIME_BETWEEN_UPDATES) {
            return true;
        } else if (locationTimeDifference < -TIME_BETWEEN_UPDATES) {
            return false;
        }

        int locationAccuracyDifference = (int) (newLocation.getAccuracy() - lastLocation.getAccuracy());
        boolean newLocationIsMoreAccurate = locationAccuracyDifference < 0;
        boolean newLocationIsSignificantlyLessAccurate = locationAccuracyDifference > 200;

        boolean newLocationIsFromSameProvider = locationIsFromSameProvider(newLocation.getProvider(), lastLocation.getProvider());

        if (newLocationIsMoreAccurate) {
            return true;
        } else if (newLocationIsMoreRecent && !newLocationIsSignificantlyLessAccurate) {
            return true;
        } else if (newLocationIsMoreRecent && !newLocationIsSignificantlyLessAccurate && newLocationIsFromSameProvider) {
            return true;
        }
        return false;
    }

    private static boolean locationIsFromSameProvider(String newLocationProvider, String lastLocationProvider) {
        if (newLocationProvider == null) {
            return lastLocationProvider == null;
        }
        return newLocationProvider.equals(lastLocationProvider);
    }
}
