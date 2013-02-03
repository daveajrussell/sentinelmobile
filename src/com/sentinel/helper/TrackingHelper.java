package com.sentinel.helper;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import com.sentinel.models.GeospatialInformation;
import com.sentinel.preferences.SentinelSharedPreferences;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * David Russell
 * 24/01/13
 */
public class TrackingHelper {
    public static GeospatialInformation getLastKnownGeospatialInformation(final Context context) {
        LocationManager oLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        List<String> arrProviders = oLocationManager.getProviders(true);

        Location oLocation = null;

        for (int i = arrProviders.size() - 1; i >= 0; i--) {
            oLocation = oLocationManager.getLastKnownLocation(arrProviders.get(i));
            if (oLocation != null)
                break;
        }

        if (oLocation != null)
            return buildGeospatialInformationObject(context, oLocation);
        else
            return null;
    }

    public static GeospatialInformation buildGeospatialInformationObject(final Context context, final Location location) {
        SentinelSharedPreferences oSentinelSharedPreferences = new SentinelSharedPreferences(context);
        return new GeospatialInformation
                (
                        oSentinelSharedPreferences.getSessionID(),
                        oSentinelSharedPreferences.getUserIdentification(),
                        Calendar.getInstance(TimeZone.getTimeZone("gmt+1")).getTimeInMillis(),
                        location.getLongitude(),
                        location.getLatitude(),
                        context.getResources().getConfiguration().orientation,
                        location.getSpeed()
                );
    }
}
