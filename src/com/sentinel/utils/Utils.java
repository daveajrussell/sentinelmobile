package com.sentinel.utils;

import android.location.Location;

import java.util.concurrent.TimeUnit;

public abstract class Utils {
    public static String getFormattedHrsMinsSecsTimeString(final long timeInMillis) {
        if (timeInMillis > 0) {
            String timeString;
            try {
                timeString = String.format("%d:%d:%d",
                        TimeUnit.MILLISECONDS.toHours(timeInMillis),
                        TimeUnit.MILLISECONDS.toMinutes(timeInMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toHours(timeInMillis)),
                        TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillis)));
            } catch (Exception ex) {
                ex.printStackTrace();
                timeString = null;
            }
            return timeString;
        } else {
            return null;
        }
    }

    public static String getFormattedMinsSecsTimeString(final long timeInMillis) {
        if (timeInMillis > 0) {
            String timeString;
            try {

                if (0 > TimeUnit.MILLISECONDS.toHours(timeInMillis)) {
                    timeString = String.format("%d:%d",
                            TimeUnit.MILLISECONDS.toMinutes(timeInMillis),
                            TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillis)));
                } else {
                    timeString = String.format("%d:%d",
                            TimeUnit.MILLISECONDS.toMinutes(timeInMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toHours(timeInMillis)),
                            TimeUnit.MILLISECONDS.toSeconds(timeInMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeInMillis)));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                timeString = null;
            }
            return timeString;
        } else {
            return null;
        }
    }

    public static boolean checkUpdateIsMoreAccurate(Location newLocation, Location currentLocation, final int TIME_BETWEEN_UPDATES) {

        final boolean MORE_ACCURATE = true;
        final boolean LESS_ACCURATE = false;

        if (null == currentLocation) {
            return MORE_ACCURATE;
        }

        long timeDifferenceBetweenLocations = newLocation.getTime() - currentLocation.getTime();
        boolean newLocationIsSignificantlyNewer = timeDifferenceBetweenLocations > TIME_BETWEEN_UPDATES;
        boolean newLocationIsSignificantlyOlder = timeDifferenceBetweenLocations < -TIME_BETWEEN_UPDATES;
        boolean newLocationIsMoreRecent = timeDifferenceBetweenLocations > 0;

        if (newLocationIsSignificantlyNewer) {
            return MORE_ACCURATE;
        } else if (newLocationIsSignificantlyOlder) {
            return LESS_ACCURATE;
        }

        int accuracyDifferenceBetweenLocations = (int) (newLocation.getAccuracy() - currentLocation.getAccuracy());
        boolean newLocationIsLessAccurate = accuracyDifferenceBetweenLocations > 0;
        boolean newLocationIsMoreAccurate = accuracyDifferenceBetweenLocations < 0;
        boolean newLocationIsSignificantlyLessAccurate = accuracyDifferenceBetweenLocations > 200;

        boolean newLocationIsFromSameProvider = locationIsFromSameProvider(newLocation.getProvider(), currentLocation.getProvider());

        if (newLocationIsMoreAccurate) {
            return MORE_ACCURATE;
        } else if (newLocationIsMoreRecent && !newLocationIsLessAccurate) {
            return MORE_ACCURATE;
        } else if (newLocationIsMoreRecent && !newLocationIsSignificantlyLessAccurate && newLocationIsFromSameProvider) {
            return MORE_ACCURATE;
        }
        return LESS_ACCURATE;
    }

    private static boolean locationIsFromSameProvider(String newLocationProvider, String currentLocationProvider) {
        if (null == newLocationProvider) {
            return null == currentLocationProvider;
        }
        return newLocationProvider.equals(currentLocationProvider);
    }
}
