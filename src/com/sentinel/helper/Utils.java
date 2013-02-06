package com.sentinel.helper;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class Utils {
    public static String getFormattedHrsMinsSecsTimeString(final long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        return simpleDateFormat.format(cal.getTime());
    }

    public static String getFormattedMinsSecsTimeString(final long timeInMillis) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeInMillis);
        SimpleDateFormat simgleDateFormat = new SimpleDateFormat("mm:ss");
        return simgleDateFormat.format(cal.getTime());
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
