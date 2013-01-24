package com.sentinel.tracking;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import com.sentinel.models.GeospatialInformation;
import com.sentinel.preferences.SentinelSharedPreferences;
import org.json.JSONStringer;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/**
 * David Russell
 * 24/01/13
 */
public class TrackingHelper
{
    public static String getLastKnowGeospatialInformationJson(Context oContext)
    {
        GeospatialInformation oInformation = getLastKnownGeospatialInformation(oContext);
        String strGeoInformationJson;

        try
        {
            strGeoInformationJson = new JSONStringer()
                    .object()
                    .key("iSessionID").value(oInformation.getSessionID())
                    .key("oUserIdentification").value(oInformation.getUserIndentification())
                    .key("lTimeStamp").value(oInformation.getDateTimeStamp())
                    .key("dLatitude").value(oInformation.getLatitude())
                    .key("dLongitude").value(oInformation.getLongitude())
                    .key("dSpeed").value(oInformation.getSpeed())
                    .key("iOrientation").value(oInformation.getOrientation())
                    .endObject().toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            strGeoInformationJson = null;
        }

        return strGeoInformationJson;
    }

    public static GeospatialInformation getLastKnownGeospatialInformation(Context oContext)
    {
        LocationManager oLocationManager = (LocationManager) oContext.getSystemService(Context.LOCATION_SERVICE);
        List<String> arrProviders = oLocationManager.getProviders(true);

        Location oLocation = null;

        for (int i = arrProviders.size()-1; i >= 0; i--)
        {

            oLocation = oLocationManager.getLastKnownLocation(arrProviders.get(i));
            if(oLocation != null)
            {
                break;
            }
        }

        return buildGeospatialInformationObject(oContext, oLocation);
    }

    public static GeospatialInformation buildGeospatialInformationObject(Context oContext, Location oCurrentLocationData)
    {
        SentinelSharedPreferences oSentinelSharedPreferences = new SentinelSharedPreferences(oContext);
        return new GeospatialInformation
                (
                        oSentinelSharedPreferences.getSessionID(),
                        oSentinelSharedPreferences.getUserIdentification(),
                        Calendar.getInstance(TimeZone.getTimeZone("gmt+1")).getTimeInMillis(),
                        oCurrentLocationData.getLongitude(),
                        oCurrentLocationData.getLatitude(),
                        oContext.getResources().getConfiguration().orientation,
                        oCurrentLocationData.getSpeed()
                );
    }
}
